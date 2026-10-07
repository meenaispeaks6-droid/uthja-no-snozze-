package com.example.sleep

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SleepLiveMetrics(
    val elapsedSeconds: Long = 0L,
    val currentDecibels: Float = 28f,
    val isSnoringNow: Boolean = false,
    val snoreEventCount: Int = 0,
    val totalSnoreSeconds: Int = 0,
    val recentAmplitudes: List<Float> = listOf(0.15f, 0.25f, 0.35f, 0.20f, 0.40f, 0.30f, 0.22f, 0.18f)
)

/**
 * Singleton state holder connecting the sleep tracking foreground service
 * with Jetpack Compose UI in LuneViewModel and SleepScreen.
 */
object SleepTrackingManager {

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _startedAt = MutableStateFlow(0L)
    val startedAt = _startedAt.asStateFlow()

    private val _liveMetrics = MutableStateFlow(SleepLiveMetrics())
    val liveMetrics = _liveMetrics.asStateFlow()

    private val _lastCompletedSessionId = MutableStateFlow<Long?>(null)
    val lastCompletedSessionId = _lastCompletedSessionId.asStateFlow()

    fun updateTrackingState(tracking: Boolean, start: Long = 0L) {
        _isTracking.value = tracking
        if (tracking) {
            _startedAt.value = start
            _liveMetrics.value = SleepLiveMetrics()
        }
    }

    fun updateMetrics(
        elapsed: Long,
        db: Float,
        snoringNow: Boolean,
        snoreCount: Int,
        snoreSeconds: Int,
        amplitudes: List<Float>
    ) {
        _liveMetrics.value = SleepLiveMetrics(
            elapsedSeconds = elapsed,
            currentDecibels = db,
            isSnoringNow = snoringNow,
            snoreEventCount = snoreCount,
            totalSnoreSeconds = snoreSeconds,
            recentAmplitudes = amplitudes
        )
    }

    fun notifySessionCompleted(sessionId: Long) {
        _lastCompletedSessionId.value = sessionId
        _isTracking.value = false
    }

    fun startTracking(context: Context) {
        val intent = Intent(context, SleepSoundTrackingService::class.java).apply {
            action = SleepSoundTrackingService.ACTION_START_TRACKING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTracking(context: Context) {
        val intent = Intent(context, SleepSoundTrackingService::class.java).apply {
            action = SleepSoundTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }
}
