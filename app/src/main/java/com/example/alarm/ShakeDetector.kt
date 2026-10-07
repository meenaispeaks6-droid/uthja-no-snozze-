package com.example.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * High-precision, responsive ShakeDetector using Android SensorManager.
 * Detects accelerometer movement, sudden jerks, direction reversals, and physical shakes
 * to allow users to reliably dismiss ringing alarms.
 */
class ShakeDetector(
    private val context: Context,
    var onMotion: ((ax: Float, ay: Float, az: Float, gForce: Float) -> Unit)? = null,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var hasInitializedSample = false

    // Highly calibrated shake sensitivity:
    // Stationary phone has gForce ~ 1.0. Any intentional shake or wrist flick produces
    // deviation from 1G or high delta acceleration between consecutive samples.
    private val minNetGForceThreshold = 0.36f // |gForce - 1.0| >= 0.36
    private val minDeltaGThreshold = 0.42f    // rapid change in acceleration
    private val absoluteGForceThreshold = 1.32f // total acceleration spike
    private val shakeCooldownMs = 190L

    var isRunning = false
        private set

    val isAvailable: Boolean
        get() = accelerometer != null

    fun start() {
        if (isRunning) return
        accelerometer?.let {
            val registered = sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) ?: false
            isRunning = registered
            hasInitializedSample = false
        }
    }

    fun stop() {
        if (!isRunning) return
        sensorManager?.unregisterListener(this)
        isRunning = false
        hasInitializedSample = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)
        val gForce = magnitude / SensorManager.GRAVITY_EARTH
        val netGForce = abs(gForce - 1.0f)

        var deltaG = 0f
        if (hasInitializedSample) {
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ
            deltaG = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / SensorManager.GRAVITY_EARTH
        } else {
            hasInitializedSample = true
        }

        lastX = x
        lastY = y
        lastZ = z

        // Stream real-time motion vector for interactive UI, meter & animations
        onMotion?.invoke(x, y, z, gForce)

        // Check if movement qualifies as an intentional shake
        val isShake = (netGForce >= minNetGForceThreshold) ||
                      (deltaG >= minDeltaGThreshold) ||
                      (gForce >= absoluteGForceThreshold)

        if (isShake) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= shakeCooldownMs) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
