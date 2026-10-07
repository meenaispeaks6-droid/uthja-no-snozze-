-- ==============================================================================
-- Migration: Create Users Profile and Real-Time Optimized Leaderboard
-- ==============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Users Table
CREATE TABLE IF NOT EXISTS public.users (
    uid UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL DEFAULT '',
    email TEXT UNIQUE,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.users REPLICA IDENTITY FULL;

CREATE INDEX IF NOT EXISTS idx_users_display_name ON public.users(display_name);
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

CREATE POLICY "Public user profiles are viewable by everyone"
    ON public.users FOR SELECT USING (true);

CREATE POLICY "Users can insert their own profile"
    ON public.users FOR INSERT WITH CHECK (auth.uid() = uid);

CREATE POLICY "Users can update their own profile"
    ON public.users FOR UPDATE USING (auth.uid() = uid) WITH CHECK (auth.uid() = uid);

-- 2. Leaderboard Table
CREATE TABLE IF NOT EXISTS public.leaderboard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(uid) ON DELETE CASCADE,
    points BIGINT NOT NULL DEFAULT 0 CHECK (points >= 0),
    total_apples_collected INTEGER NOT NULL DEFAULT 0 CHECK (total_apples_collected >= 0),
    streak_count INTEGER NOT NULL DEFAULT 0 CHECK (streak_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_leaderboard_user UNIQUE (user_id)
);

ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaderboard REPLICA IDENTITY FULL;

-- 3. Indexes for Real-Time & Query Performance
CREATE INDEX IF NOT EXISTS idx_leaderboard_ranking 
    ON public.leaderboard (points DESC, total_apples_collected DESC, streak_count DESC);

CREATE INDEX IF NOT EXISTS idx_leaderboard_user_covering 
    ON public.leaderboard (user_id) 
    INCLUDE (points, total_apples_collected, streak_count, updated_at);

-- 4. Leaderboard RLS Policies
CREATE POLICY "Leaderboard entries are viewable by everyone"
    ON public.leaderboard FOR SELECT USING (true);

CREATE POLICY "Users can insert their own leaderboard record"
    ON public.leaderboard FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own leaderboard record"
    ON public.leaderboard FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- 5. Realtime Publication Setup
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'leaderboard'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.leaderboard;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'users'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.users;
    END IF;
END $$;

-- 6. Trigger: Updated At
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE OR REPLACE TRIGGER trg_leaderboard_updated_at
    BEFORE UPDATE ON public.leaderboard
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- 7. Trigger: Automatic User & Leaderboard Profile Setup on Signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    raw_name TEXT;
BEGIN
    raw_name := COALESCE(
        NEW.raw_user_meta_data->>'display_name',
        NEW.raw_user_meta_data->>'full_name',
        NEW.raw_user_meta_data->>'name',
        SPLIT_PART(NEW.email, '@', 1),
        'Player'
    );

    INSERT INTO public.users (uid, display_name, email, avatar_url)
    VALUES (NEW.id, raw_name, NEW.email, NEW.raw_user_meta_data->>'avatar_url')
    ON CONFLICT (uid) DO UPDATE
    SET 
        email = EXCLUDED.email,
        display_name = CASE WHEN public.users.display_name = '' THEN EXCLUDED.display_name ELSE public.users.display_name END;

    INSERT INTO public.leaderboard (user_id, points, total_apples_collected, streak_count)
    VALUES (NEW.id, 0, 0, 0)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 8. Stored Procedure: Atomic Real-Time Increments
CREATE OR REPLACE FUNCTION public.increment_player_stats(
    p_user_id UUID DEFAULT auth.uid(),
    p_points_delta BIGINT DEFAULT 0,
    p_apples_delta INTEGER DEFAULT 0,
    p_streak_override INTEGER DEFAULT NULL
)
RETURNS public.leaderboard AS $$
DECLARE
    result_row public.leaderboard;
BEGIN
    IF p_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated or user ID missing';
    END IF;

    INSERT INTO public.leaderboard (user_id, points, total_apples_collected, streak_count)
    VALUES (
        p_user_id, 
        GREATEST(0, p_points_delta), 
        GREATEST(0, p_apples_delta), 
        COALESCE(p_streak_override, 1)
    )
    ON CONFLICT (user_id) DO UPDATE
    SET 
        points = GREATEST(0, public.leaderboard.points + EXCLUDED.points),
        total_apples_collected = GREATEST(0, public.leaderboard.total_apples_collected + EXCLUDED.total_apples_collected),
        streak_count = CASE 
            WHEN p_streak_override IS NOT NULL THEN p_streak_override
            ELSE public.leaderboard.streak_count + 1
        END,
        updated_at = timezone('utc'::text, now())
    RETURNING * INTO result_row;

    RETURN result_row;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 9. Ranked View
CREATE OR REPLACE VIEW public.leaderboard_ranks AS
SELECT
    DENSE_RANK() OVER (
        ORDER BY l.points DESC, l.total_apples_collected DESC, l.streak_count DESC
    ) AS rank,
    l.id AS leaderboard_id,
    l.user_id,
    u.display_name,
    u.avatar_url,
    l.points,
    l.total_apples_collected,
    l.streak_count,
    l.updated_at
FROM public.leaderboard l
JOIN public.users u ON l.user_id = u.uid;

GRANT SELECT ON public.leaderboard_ranks TO anon, authenticated;
GRANT ALL ON public.users TO authenticated;
GRANT SELECT ON public.users TO anon;
GRANT ALL ON public.leaderboard TO authenticated;
GRANT SELECT ON public.leaderboard TO anon;
GRANT EXECUTE ON FUNCTION public.increment_player_stats TO authenticated;
