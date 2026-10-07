package com.example.data.supabase

import com.example.data.model.AlarmEntity
import com.example.data.model.SleepSessionEntity
import com.example.data.model.UserProfileEntity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SupabaseRepository {

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    val sessionStatus: Flow<SessionStatus>
        get() {
            val client = SupabaseClientProvider.getClient() ?: return flowOf(SessionStatus.NotAuthenticated(false))
            return client.auth.sessionStatus
        }

    fun currentUserOrNull(): UserInfo? {
        val client = SupabaseClientProvider.getClient() ?: return null
        return try {
            client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signUpWithEmail(emailInput: String, passwordInput: String): Result<Unit> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase is not configured yet. Add SUPABASE_URL and SUPABASE_ANON_KEY to .env or Secrets."))

        return try {
            client.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(emailInput: String, passwordInput: String): Result<Unit> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase is not configured yet. Add SUPABASE_URL and SUPABASE_ANON_KEY to .env or Secrets."))

        return try {
            client.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase is not configured"))

        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun backupAlarms(alarms: List<AlarmEntity>): Result<Int> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val userId = client.auth.currentUserOrNull()?.id
            val dtoList = alarms.map { it.toSupabaseDto(userId) }
            if (dtoList.isNotEmpty()) {
                client.postgrest["alarms"].upsert(dtoList)
            }
            Result.success(dtoList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAlarms(): Result<List<AlarmEntity>> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val response = client.postgrest["alarms"].select().decodeList<SupabaseAlarmDto>()
            val entities = response.map { it.toEntity() }
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun backupSleepSessions(sessions: List<SleepSessionEntity>): Result<Int> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val userId = client.auth.currentUserOrNull()?.id
            val dtoList = sessions.map { it.toSupabaseDto(userId) }
            if (dtoList.isNotEmpty()) {
                client.postgrest["sleep_sessions"].upsert(dtoList)
            }
            Result.success(dtoList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSleepSessions(): Result<List<SleepSessionEntity>> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val response = client.postgrest["sleep_sessions"].select().decodeList<SupabaseSleepSessionDto>()
            val entities = response.map { it.toEntity() }
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun backupProfile(profile: UserProfileEntity): Result<Unit> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val userId = client.auth.currentUserOrNull()?.id
            val dto = profile.toSupabaseDto(userId)
            client.postgrest["user_profile"].upsert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProfile(currentProfile: UserProfileEntity): Result<UserProfileEntity?> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val dto = client.postgrest["user_profile"].select().decodeSingleOrNull<SupabaseProfileDto>()
            Result.success(dto?.toEntity(currentProfile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLeaderboard(limitCount: Long = 50): Result<List<SupabaseLeaderboardRankDto>> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val list = client.postgrest["leaderboard_ranks"]
                .select {
                    limit(limitCount)
                }
                .decodeList<SupabaseLeaderboardRankDto>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserLeaderboard(userId: String): Result<SupabaseLeaderboardDto?> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val entry = client.postgrest["leaderboard"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<SupabaseLeaderboardDto>()
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertLeaderboard(
        userId: String,
        points: Long,
        apples: Int,
        streak: Int
    ): Result<Unit> {
        val client = SupabaseClientProvider.getClient()
            ?: return Result.failure(IllegalStateException("Supabase not configured"))

        return try {
            val dto = SupabaseLeaderboardDto(
                userId = userId,
                points = points,
                totalApplesCollected = apples,
                streakCount = streak
            )
            client.postgrest["leaderboard"].upsert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
