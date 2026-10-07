package com.example.sleep

import com.example.data.model.SnoreEventEntity
import kotlin.math.max

/**
 * Aggregates nearby positive snore classifications into distinct, structured
 * snoring events with start/end timestamps, duration, peak confidence score,
 * and average decibels.
 */
class SnoreEventAggregator {

    private val recordedEvents = mutableListOf<SnoreEventEntity>()

    // Current candidate event tracking
    private var eventStartMs: Long? = null
    private var lastDetectionMs: Long? = null
    private var peakConfidence: Float = 0f
    private val currentDecibels = mutableListOf<Float>()

    // Cooldown window: if no snoring is detected for 12 seconds, close current event
    private val cooldownWindowMs = 12_000L

    /**
     * Process classification result at a specific timestamp.
     */
    @Synchronized
    fun processResult(result: AudioClassificationResult, timestampMs: Long) {
        if (result.isSnoring) {
            val start = eventStartMs
            if (start == null) {
                // Begin new snoring event
                eventStartMs = timestampMs
                lastDetectionMs = timestampMs
                peakConfidence = result.snoreConfidence
                currentDecibels.clear()
                currentDecibels.add(result.decibels)
            } else {
                // Continue active snoring event
                lastDetectionMs = timestampMs
                peakConfidence = max(peakConfidence, result.snoreConfidence)
                currentDecibels.add(result.decibels)
            }
        } else {
            // Check if cooldown elapsed to finalize active event
            val start = eventStartMs
            val last = lastDetectionMs
            if (start != null && last != null) {
                if (timestampMs - last >= cooldownWindowMs) {
                    finalizeCurrentEvent(last)
                }
            }
        }
    }

    /**
     * Finalizes current in-flight event if it has lasted at least 1.5 seconds.
     */
    @Synchronized
    private fun finalizeCurrentEvent(endTimestampMs: Long) {
        val start = eventStartMs ?: return
        val durationSeconds = max(1, ((endTimestampMs - start) / 1000L).toInt())
        val avgDb = if (currentDecibels.isNotEmpty()) currentDecibels.average().toFloat() else 45f

        val event = SnoreEventEntity(
            id = 0,
            sessionId = 0, // Assigned upon database persist
            startTimestamp = start,
            endTimestamp = endTimestampMs,
            durationSeconds = durationSeconds,
            peakScore = peakConfidence,
            avgDecibels = avgDb,
            soundLabel = "Snoring"
        )
        recordedEvents.add(event)

        // Reset current tracking
        eventStartMs = null
        lastDetectionMs = null
        peakConfidence = 0f
        currentDecibels.clear()
    }

    /**
     * Finishes any pending event at the end of the sleep tracking session.
     */
    @Synchronized
    fun finishSession(endTimestampMs: Long): List<SnoreEventEntity> {
        val start = eventStartMs
        if (start != null) {
            val last = lastDetectionMs ?: endTimestampMs
            finalizeCurrentEvent(last)
        }
        return recordedEvents.toList()
    }

    @Synchronized
    fun getRecordedEvents(): List<SnoreEventEntity> = recordedEvents.toList()

    @Synchronized
    fun getTotalSnoreSeconds(): Int = recordedEvents.sumOf { it.durationSeconds }

    @Synchronized
    fun getSnoreCount(): Int = recordedEvents.size

    @Synchronized
    fun getPeakDecibels(): Float = recordedEvents.maxOfOrNull { it.avgDecibels } ?: 0f

    @Synchronized
    fun getAvgDecibels(): Float {
        return if (recordedEvents.isNotEmpty()) {
            recordedEvents.map { it.avgDecibels }.average().toFloat()
        } else {
            0f
        }
    }
}
