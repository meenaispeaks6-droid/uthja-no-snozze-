package com.example.sleep

import android.content.Context
import android.media.AudioFormat
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.core.BaseOptions
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Result of on-device audio classification frame.
 */
data class AudioClassificationResult(
    val isSnoring: Boolean,
    val snoreConfidence: Float,
    val topCategory: String,
    val decibels: Float
)

/**
 * Offline on-device sleep audio classifier using Google MediaPipe Audio Classifier
 * with bundled YAMNet model (521 AudioSet categories including "Snoring").
 *
 * Runs 100% locally on device. No internet, no audio recording saved to disk.
 */
class SnoreAudioClassifier(private val context: Context) : AutoCloseable {

    private val tag = "SnoreAudioClassifier"
    private var classifier: AudioClassifier? = null
    private val modelFileName = "yamnet.tflite"

    init {
        initializeClassifier()
    }

    private fun initializeClassifier() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelFileName)
                .build()

            val options = AudioClassifier.AudioClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(6)
                .setScoreThreshold(0.20f)
                .build()

            classifier = AudioClassifier.createFromOptions(context, options)
            Log.i(tag, "MediaPipe AudioClassifier successfully initialized with $modelFileName")
        } catch (e: Throwable) {
            Log.e(tag, "Could not initialize MediaPipe AudioClassifier: ${e.message}", e)
            classifier = null
        }
    }

    /**
     * Classifies a 16-bit PCM audio buffer (16 kHz mono).
     */
    fun classify(pcmBuffer: ShortArray, readLength: Int, sampleRate: Int = 16000): AudioClassificationResult {
        val rms = calculateRms(pcmBuffer, readLength)
        val decibels = calculateDecibels(rms)

        var isSnoring = false
        var snoreConfidence = 0f
        var topCategory = "Silence"

        val activeClassifier = classifier
        if (activeClassifier != null && readLength > 0) {
            try {
                val format = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()

                val audioData = AudioData.create(format, readLength)
                audioData.load(pcmBuffer, 0, readLength)

                val result: AudioClassifierResult = activeClassifier.classify(audioData)
                val classificationResults = result.classificationResults()

                for (classifResult in classificationResults) {
                    for (head in classifResult.classifications()) {
                        for (category in head.categories()) {
                            val name = category.categoryName() ?: ""
                            val score = category.score()

                            if (topCategory == "Silence" && score > 0.35f) {
                                topCategory = name
                            }

                            if (name.equals("Snoring", ignoreCase = true)) {
                                if (score > snoreConfidence) {
                                    snoreConfidence = score
                                }
                            }
                        }
                    }
                }

                // If snoring confidence exceeds threshold
                if (snoreConfidence >= 0.38f) {
                    isSnoring = true
                    topCategory = "Snoring"
                }
            } catch (e: Throwable) {
                Log.w(tag, "Classification inference warning: ${e.message}")
            }
        } else {
            // Acoustic energy heuristic fallback if model fails to load or low RAM
            if (decibels > 48f && rms > 650f) {
                snoreConfidence = (decibels / 100f).coerceIn(0.35f, 0.75f)
                isSnoring = snoreConfidence >= 0.50f
                topCategory = if (isSnoring) "Snoring" else "Sound"
            }
        }

        return AudioClassificationResult(
            isSnoring = isSnoring,
            snoreConfidence = snoreConfidence,
            topCategory = topCategory,
            decibels = decibels
        )
    }

    private fun calculateRms(buffer: ShortArray, length: Int): Float {
        if (length <= 0) return 0f
        var sumSquares = 0.0
        for (i in 0 until length) {
            val v = buffer[i].toDouble()
            sumSquares += v * v
        }
        return sqrt(sumSquares / length).toFloat()
    }

    private fun calculateDecibels(rms: Float): Float {
        if (rms <= 1f) return 25f
        val ref = 32767.0
        val db = 20.0 * log10((rms / ref).coerceAtLeast(1e-5)) + 90.0
        return db.toFloat().coerceIn(25f, 95f)
    }

    override fun close() {
        try {
            classifier?.close()
        } catch (e: Exception) {
            Log.e(tag, "Error closing classifier", e)
        }
        classifier = null
    }
}
