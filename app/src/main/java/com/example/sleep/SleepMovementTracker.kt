package com.example.sleep

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

data class MovementEvent(
    val timestamp: Long,
    val intensity: Float, // 0.0f - 1.0f
    val isRestless: Boolean
)

data class SleepTrackingData(
    val isTracking: Boolean = false,
    val startedAt: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val movementCount: Int = 0,
    val currentMotionLevel: Float = 0f, // 0.0f (still) to 1.0f (high motion)
    val lastMovementTime: Long = 0L,
    val restlessSpikeCount: Int = 0,
    val recentEvents: List<MovementEvent> = emptyList()
) {
    val durationMinutes: Int
        get() = (elapsedSeconds / 60L).toInt().coerceAtLeast(1)

    fun calculateDeepSleepMinutes(): Int {
        val total = durationMinutes
        val restless = calculateRestlessMinutes()
        val stillRatio = ((total - restless).toFloat() / total).coerceIn(0.2f, 0.85f)
        return (total * stillRatio * 0.55f).toInt().coerceIn(0, total)
    }

    fun calculateRestlessMinutes(): Int {
        return (restlessSpikeCount * 2).coerceAtMost((durationMinutes / 2).coerceAtLeast(1))
    }

    fun calculateLightSleepMinutes(): Int {
        val total = durationMinutes
        val deep = calculateDeepSleepMinutes()
        val restless = calculateRestlessMinutes()
        return (total - deep - restless).coerceAtLeast(0)
    }

    fun calculateSleepScore(goalMinutes: Int = 480): Int {
        val durationScore = ((durationMinutes.toFloat() / goalMinutes) * 50f).coerceIn(10f, 50f)
        val totalMovements = movementCount
        val hours = durationMinutes / 60f
        val movementsPerHour = if (hours > 0.1f) totalMovements / hours else totalMovements.toFloat()
        val restfulnessScore = (50f - (movementsPerHour * 3.2f)).coerceIn(15f, 50f)
        return (durationScore + restfulnessScore).toInt().coerceIn(35, 99)
    }

    fun calculateMovementRating(): String {
        val hours = durationMinutes / 60f
        val movementsPerHour = if (hours > 0.1f) movementCount / hours else movementCount.toFloat()
        return when {
            movementsPerHour <= 2.5f -> "Very Peaceful"
            movementsPerHour <= 6.0f -> "Calm with Light Shifts"
            movementsPerHour <= 12.0f -> "Moderate Movement"
            else -> "Restless Sleep"
        }
    }
}

class SleepMovementTracker(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _trackingData = MutableStateFlow(SleepTrackingData())
    val trackingData: StateFlow<SleepTrackingData> = _trackingData.asStateFlow()

    private var lastMovementRegisteredMs = 0L
    private val movementCooldownMs = 2200L
    private val motionThreshold = 0.08f
    private val restlessThreshold = 0.22f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            if (!_trackingData.value.isTracking) return

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gForce = sqrt((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
            val delta = abs(gForce - 1.0f)

            val motionLevel = (delta / 0.5f).coerceIn(0f, 1f)
            val now = System.currentTimeMillis()

            if (delta >= motionThreshold) {
                if (now - lastMovementRegisteredMs >= movementCooldownMs) {
                    lastMovementRegisteredMs = now
                    val isRestless = delta >= restlessThreshold
                    val newEvent = MovementEvent(
                        timestamp = now,
                        intensity = motionLevel,
                        isRestless = isRestless
                    )
                    val current = _trackingData.value
                    val updatedEvents = (current.recentEvents + newEvent).takeLast(30)
                    _trackingData.value = current.copy(
                        movementCount = current.movementCount + 1,
                        restlessSpikeCount = if (isRestless) current.restlessSpikeCount + 1 else current.restlessSpikeCount,
                        currentMotionLevel = motionLevel,
                        lastMovementTime = now,
                        recentEvents = updatedEvents
                    )
                } else {
                    _trackingData.value = _trackingData.value.copy(currentMotionLevel = motionLevel)
                }
            } else {
                val current = _trackingData.value
                val decayed = (current.currentMotionLevel * 0.85f).coerceAtLeast(0f)
                if (abs(decayed - current.currentMotionLevel) > 0.01f) {
                    _trackingData.value = current.copy(currentMotionLevel = decayed)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startTracking(startTime: Long = System.currentTimeMillis()) {
        _trackingData.value = SleepTrackingData(
            isTracking = true,
            startedAt = startTime,
            elapsedSeconds = 0L
        )
        lastMovementRegisteredMs = 0L
        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun updateElapsedSeconds(elapsed: Long) {
        if (_trackingData.value.isTracking) {
            _trackingData.value = _trackingData.value.copy(elapsedSeconds = elapsed)
        }
    }

    fun stopTracking(): SleepTrackingData {
        val finalData = _trackingData.value
        sensorManager?.unregisterListener(sensorListener)
        _trackingData.value = SleepTrackingData(isTracking = false)
        return finalData
    }

    fun simulateMovement(isRestless: Boolean = false) {
        val now = System.currentTimeMillis()
        val current = _trackingData.value
        val newEvent = MovementEvent(
            timestamp = now,
            intensity = if (isRestless) 0.75f else 0.35f,
            isRestless = isRestless
        )
        _trackingData.value = current.copy(
            movementCount = current.movementCount + 1,
            restlessSpikeCount = if (isRestless) current.restlessSpikeCount + 1 else current.restlessSpikeCount,
            currentMotionLevel = if (isRestless) 0.8f else 0.4f,
            lastMovementTime = now,
            recentEvents = (current.recentEvents + newEvent).takeLast(30)
        )
    }

    fun simulateOvernightData(durationMinutes: Int = 465, movements: Int = 7): SleepTrackingData {
        val now = System.currentTimeMillis()
        val start = now - (durationMinutes * 60_000L)
        val restlessSpikes = (movements * 0.35f).toInt().coerceAtLeast(1)
        val events = List(movements) { i ->
            MovementEvent(
                timestamp = start + (i * (durationMinutes * 60_000L / movements)),
                intensity = if (i % 3 == 0) 0.7f else 0.35f,
                isRestless = i % 3 == 0
            )
        }
        val data = SleepTrackingData(
            isTracking = true,
            startedAt = start,
            elapsedSeconds = durationMinutes * 60L,
            movementCount = movements,
            restlessSpikeCount = restlessSpikes,
            currentMotionLevel = 0f,
            lastMovementTime = now - 600_000L,
            recentEvents = events
        )
        _trackingData.value = data
        return data
    }
}
