package com.example.alarm

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class HapticsController(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Apple Taptic Engine Style - Medium impact button click.
     * Replicates iOS UIImpactFeedbackGenerator(style: .medium).
     * Used for Primary "Next" buttons, Call-to-actions, and main confirmations.
     */
    fun pulseAppleButtonClick() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (v.hasAmplitudeControl()) {
                    // Ultra-crisp 14ms mechanical pulse with high amplitude
                    v.vibrate(VibrationEffect.createOneShot(14L, 205))
                } else {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(15L, 190))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(14L)
            }
        }
    }

    /**
     * Apple Taptic Engine Style - Light selection tick.
     * Replicates iOS UISelectionFeedbackGenerator / UIImpactFeedbackGenerator(style: .light).
     * Used for selecting missions (Math, Color Tiles, etc.), toggles, switches, tabs, and chips.
     */
    fun pulseAppleSelection() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (v.hasAmplitudeControl()) {
                    // Crisp, subtle 8ms tick with moderate amplitude
                    v.vibrate(VibrationEffect.createOneShot(8L, 120))
                } else {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(10L, 110))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(8L)
            }
        }
    }

    /**
     * Apple Taptic Engine Style - Success double-tap confirmation.
     * Replicates iOS UINotificationFeedbackGenerator(type: .success).
     * Used for completing onboarding or successfully granting a permission.
     */
    fun pulseAppleSuccess() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 12, 50, 18)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && v.hasAmplitudeControl()) {
                    val amplitudes = intArrayOf(0, 140, 0, 220)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    v.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 12, 50, 18), -1)
            }
        }
    }

    fun pulseShake() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (v.hasAmplitudeControl()) {
                    v.vibrate(VibrationEffect.createOneShot(45L, 235))
                } else {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(45L)
            }
        }
    }

    /**
     * Subtle tactile confirmation when the ball hits the edge of the screen.
     * Calibrated to provide a crisp, mechanical edge tap scaled by impact speed.
     */
    fun pulseEdgeBounce(intensity: Float = 0.5f) {
        vibrator?.let { v ->
            val ratio = intensity.coerceIn(0.15f, 1.0f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (v.hasAmplitudeControl()) {
                    val amplitude = (ratio * 135f).toInt().coerceIn(25, 175)
                    val durationMs = (10L + (ratio * 12f).toLong()).coerceIn(10L, 24L)
                    v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
                } else {
                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (ratio * 135f).toInt().coerceIn(25, 175)
                v.vibrate(VibrationEffect.createOneShot(16L, amplitude))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(15L)
            }
        }
    }

    fun pulseError() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 80, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 80, 80, 80), -1)
            }
        }
    }

    fun pulseSuccess() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 180), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 100, 50, 180), -1)
            }
        }
    }

    fun startAlarmVibration() {
        vibrator?.let {
            val timings = longArrayOf(0, 500, 300, 500, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(timings, 0))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(timings, 0)
            }
        }
    }

    fun stopAlarmVibration() {
        vibrator?.cancel()
    }
}

/**
 * Provides an instance of [HapticsController] remembered across recompositions.
 */
@Composable
fun rememberAppleHaptics(): HapticsController {
    val context = LocalContext.current
    return remember(context) { HapticsController(context) }
}

