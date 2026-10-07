package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class WakeMode(val title: String, val description: String) {
    GENTLE("Gentle", "Soft tone, calm progression, light challenges"),
    FOCUS("Focus", "Balanced alarm tone, standard challenge, strict snooze"),
    BEAST("Beast", "Max volume siren, multi-step math or heavy shake, zero snooze")
}

enum class DismissType(val title: String, val iconName: String) {
    MATH("Math Challenge", "calculate"),
    COLOR_TILES("Find Color Tiles", "palette"),
    TYPING("Typing", "keyboard"),
    SHAKE("Physical Shake", "vibration"),
    OFF("Off", "alarm_off"),
    BOTH("Full Discipline (Both)", "all_inclusive")
}

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: String = "DAILY", // comma-separated or "DAILY" / "ONCE"
    val isEnabled: Boolean = true,
    val wakeMode: WakeMode = WakeMode.FOCUS,
    val dismissType: DismissType = DismissType.MATH,
    val sound: String = "Good Morningggg",
    val wallpaper: String = "wp_aesthetic_1",
    val strictNoSnooze: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "alarm_events")
data class AlarmEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val triggeredAt: Long,
    val dismissedAt: Long,
    val dismissDurationSeconds: Int,
    val dismissType: DismissType,
    val attempts: Int,
    val success: Boolean,
    val wakeMode: WakeMode
)

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val sleepGoalMet: Boolean,
    val source: String = "AUDIO_TRACKING",
    val notes: String = "",
    val movementCount: Int = 0,
    val deepSleepMinutes: Int = 0,
    val lightSleepMinutes: Int = 0,
    val restlessMinutes: Int = 0,
    val sleepScore: Int = 85,
    val movementRating: String = "Peaceful",
    val snoreEventCount: Int = 0,
    val snoreDurationMinutes: Int = 0,
    val snorePercentage: Int = 0,
    val avgDecibels: Float = 0f,
    val peakDecibels: Float = 0f
)

@Entity(
    tableName = "snore_events",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SnoreEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationSeconds: Int,
    val peakScore: Float,
    val avgDecibels: Float = 0f,
    val soundLabel: String = "Snoring"
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String? = null,
    val score: Int = 0,
    val streakDays: Int = 0,
    val applesCount: Int = 0,
    val isCurrentUser: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Morning Visionary",
    val sleepGoalMinutes: Int = 480, // 8 hours
    val defaultWakeMode: WakeMode = WakeMode.FOCUS,
    val wakeDifficulty: String = "Medium", // Light, Medium, Deep Sleeper
    val strictNoSnoozeDefault: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val selectedAlarmHour: Int = 7,
    val selectedAlarmMinute: Int = 0,
    val selectedWallpaper: String = "wp_aesthetic_1",
    val selectedVibe: String = "Good Morningggg",
    val selectedMission: String = "Math",
    val onboardingStep: Int = 1,
    val email: String? = null,
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val streakDays: Int = 0,
    val applesCount: Int = 0,
    val leaderboardScore: Int = 0,
    val lastCompletedDate: String? = null
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timePill: String? = null,
    val iconType: String = "WATER", // WATER, MINDFUL, MOON, BOOK, WORKOUT, SUN, COFFEE, CUSTOM
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

