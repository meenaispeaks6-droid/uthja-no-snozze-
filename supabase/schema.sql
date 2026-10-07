-- ==============================================================================
-- Supabase Schema: Users Profile & Real-Time Optimized Leaderboard
-- ==============================================================================
-- Features included:
-- 1. 'users' table linked with Supabase Auth (auth.users)
-- 2. 'leaderboard' table tracking points, total_apples_collected, and streak_count
-- 3. Supabase Realtime publication configuration & REPLICA IDENTITY FULL
-- 4. High-performance composite & covering indexes for low-latency realtime queries
-- 5. Row Level Security (RLS) policies for authenticated & public access
-- 6. Automated trigger to create profile & leaderboard entry upon user signup
-- 7. Automated updated_at timestamp triggers
-- 8. Atomic stored procedure (RPC) for race-condition-free real-time stat updates
-- 9. Ranked leaderboard view with tie-breaker sorting
-- ==============================================================================

-- 1. Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==============================================================================
-- 2. USERS TABLE
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.users (
    uid UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL DEFAULT '',
    email TEXT UNIQUE,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Enable Row Level Security
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

-- Real-time optimization: REPLICA IDENTITY FULL allows Realtime to receive old & new row data
ALTER TABLE public.users REPLICA IDENTITY FULL;

-- Indexes for users
CREATE INDEX IF NOT EXISTS idx_users_display_name ON public.users(display_name);
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

-- Users RLS Policies
CREATE POLICY "Public user profiles are viewable by everyone"
    ON public.users
    FOR SELECT
    USING (true);

CREATE POLICY "Users can insert their own profile"
    ON public.users
    FOR INSERT
    WITH CHECK (auth.uid() = uid);

CREATE POLICY "Users can update their own profile"
    ON public.users
    FOR UPDATE
    USING (auth.uid() = uid)
    WITH CHECK (auth.uid() = uid);

-- ==============================================================================
-- 3. LEADERBOARD TABLE
-- ==============================================================================
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

-- Enable Row Level Security
ALTER TABLE public.leaderboard ENABLE ROW LEVEL SECURITY;

-- Real-time optimization: REPLICA IDENTITY FULL ensures UPDATE / DELETE events
-- broadcast both previous and current state across Supabase Realtime channels.
ALTER TABLE public.leaderboard REPLICA IDENTITY FULL;

-- ==============================================================================
-- 4. REAL-TIME OPTIMIZED INDEXES
-- ==============================================================================
-- High-speed multi-column B-tree index for instant leaderboard sorting and top-N queries
CREATE INDEX IF NOT EXISTS idx_leaderboard_ranking 
    ON public.leaderboard (points DESC, total_apples_collected DESC, streak_count DESC);

-- Covering index on user_id including metric columns for zero-heap real-time lookups
CREATE INDEX IF NOT EXISTS idx_leaderboard_user_covering 
    ON public.leaderboard (user_id) 
    INCLUDE (points, total_apples_collected, streak_count, updated_at);

-- ==============================================================================
-- 5. LEADERBOARD RLS POLICIES
-- ==============================================================================
CREATE POLICY "Leaderboard entries are viewable by everyone"
    ON public.leaderboard
    FOR SELECT
    USING (true);

CREATE POLICY "Users can insert their own leaderboard record"
    ON public.leaderboard
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own leaderboard record"
    ON public.leaderboard
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ==============================================================================
-- 6. ENABLE SUPABASE REALTIME REPLICATION
-- ==============================================================================
-- Ensure the supabase_realtime publication includes our tables
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

-- ==============================================================================
-- 7. AUTOMATED UPDATED_AT TRIGGER
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_updated_at();

CREATE OR REPLACE TRIGGER trg_leaderboard_updated_at
    BEFORE UPDATE ON public.leaderboard
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_updated_at();

-- ==============================================================================
-- 8. AUTOMATIC USER & LEADERBOARD INITIALIZATION ON SIGNUP
-- ==============================================================================
-- Automatically provisions a public.users profile and initial public.leaderboard entry
-- whenever a new user signs up through Supabase Auth (auth.users).
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    raw_name TEXT;
BEGIN
    -- Extract display name from user metadata or fallback to email prefix
    raw_name := COALESCE(
        NEW.raw_user_meta_data->>'display_name',
        NEW.raw_user_meta_data->>'full_name',
        NEW.raw_user_meta_data->>'name',
        SPLIT_PART(NEW.email, '@', 1),
        'Player'
    );

    -- 1. Insert user profile
    INSERT INTO public.users (uid, display_name, email, avatar_url)
    VALUES (
        NEW.id,
        raw_name,
        NEW.email,
        NEW.raw_user_meta_data->>'avatar_url'
    )
    ON CONFLICT (uid) DO UPDATE
    SET 
        email = EXCLUDED.email,
        display_name = CASE WHEN public.users.display_name = '' THEN EXCLUDED.display_name ELSE public.users.display_name END;

    -- 2. Initialize leaderboard entry
    INSERT INTO public.leaderboard (user_id, points, total_apples_collected, streak_count)
    VALUES (NEW.id, 0, 0, 0)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to listen for new user registration in auth.users
CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- 9. ATOMIC REAL-TIME STATS UPDATE RPC (Prevents Race Conditions)
-- ==============================================================================
-- Call via supabase.rpc('increment_player_stats', { p_points_delta: 50, p_apples_delta: 5, p_streak_increment: 1 })
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

    -- Upsert with atomic increment
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

-- ==============================================================================
-- 10. REAL-TIME LEADERBOARD VIEW WITH RANKS
-- ==============================================================================
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

-- Grant access to public and authenticated users
GRANT SELECT ON public.leaderboard_ranks TO anon, authenticated;
GRANT ALL ON public.users TO authenticated;
GRANT SELECT ON public.users TO anon;
GRANT ALL ON public.leaderboard TO authenticated;
GRANT SELECT ON public.leaderboard TO anon;
GRANT EXECUTE ON FUNCTION public.increment_player_stats TO authenticated;
