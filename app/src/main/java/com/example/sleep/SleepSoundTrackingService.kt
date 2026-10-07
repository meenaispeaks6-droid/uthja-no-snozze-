package com.example.sleep

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.LuneDatabase
import com.example.data.model.SleepSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Foreground Service for offline sleep sound and snoring tracking.
 *
 * Captures audio via Android AudioRecord in small overnight buffers,
 * running MediaPipe Audio Classifier (YAMNet) locally on-device.
 *
 * Adheres strictly to Android 14+ foreground service microphone rules.
 * Never uploads or permanently stores raw audio.
 */
class SleepSoundTrackingService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "com.example.sleep.ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.sleep.ACTION_STOP_TRACKING"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "sleep_sound_tracking_channel"
        private const val TAG = "SleepSoundService"
        private const val SAMPLE_RATE = 16000
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var audioRecord: AudioRecord? = null
    private var classifier: SnoreAudioClassifier? = null
    private val aggregator = SnoreEventAggregator()

    private var sessionStartTime: Long = 0L
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TRACKING

        when (action) {
            ACTION_STOP_TRACKING -> {
                Log.d(TAG, "Received ACTION_STOP_TRACKING")
                stopTrackingSession()
            }
            ACTION_START_TRACKING -> {
                if (!isRecording) {
                    startTrackingSession()
                } else {
                    Log.d(TAG, "Already tracking; ignoring duplicate start")
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startTrackingSession() {
        // Verify audio recording permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Cannot start tracking: RECORD_AUDIO permission not granted")
            stopSelf()
            return
        }

        sessionStartTime = System.currentTimeMillis()
        isRecording = true
        SleepTrackingManager.updateTrackingState(true, sessionStartTime)

        // Show persistent foreground notification with Stop action
        val notification = buildTrackingNotification("Tracking started", "Analyzing sleep sounds locally...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Acquire partial wake lock to keep CPU active when screen is off
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lune::SleepSoundWakeLock").apply {
                acquire(10 * 3600 * 1000L) // Max 10 hours safety timeout
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock: ${e.message}")
        }

        // Initialize on-device classifier
        classifier = SnoreAudioClassifier(applicationContext)

        // Launch audio recording & classification loop
        trackingJob = serviceScope.launch {
            runAudioTrackingLoop()
        }
    }

    private suspend fun runAudioTrackingLoop() = kotlinx.coroutines.coroutineScope {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        // 1-second analysis window (16,000 samples @ 16 kHz)
        val windowSize = 16000
        val bufferSize = max(minBufferSize * 2, windowSize * 2)

        if (ActivityCompat.checkSelfPermission(this@SleepSoundTrackingService, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopTrackingSession()
            return@coroutineScope
        }

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            audioRecord = record

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                stopTrackingSession()
                return@coroutineScope
            }

            record.startRecording()
            Log.i(TAG, "AudioRecord successfully started recording")

            val pcmBuffer = ShortArray(windowSize)
            val amplitudeWindow = ArrayDeque<Float>(8)
            repeat(8) { amplitudeWindow.add(0.2f) }

            var lastNotificationUpdate = System.currentTimeMillis()

            while (isActive && isRecording) {
                // Read 1-second PCM audio buffer
                var totalRead = 0
                while (totalRead < windowSize && isActive && isRecording) {
                    val read = record.read(pcmBuffer, totalRead, windowSize - totalRead)
                    if (read > 0) {
                        totalRead += read
                    } else {
                        break
                    }
                }

                if (totalRead > 0) {
                    val timestamp = System.currentTimeMillis()
                    val result = classifier?.classify(pcmBuffer, totalRead, SAMPLE_RATE)
                        ?: AudioClassificationResult(false, 0f, "Unknown", 30f)

                    // Aggregate into snoring events
                    aggregator.processResult(result, timestamp)

                    // Compute normalized visual amplitude for waveform
                    val normAmp = ((result.decibels - 30f) / 60f).coerceIn(0.1f, 1.0f)
                    amplitudeWindow.removeFirst()
                    amplitudeWindow.add(normAmp)

                    val elapsed = (timestamp - sessionStartTime) / 1000L
                    SleepTrackingManager.updateMetrics(
                        elapsed = elapsed,
                        db = result.decibels,
                        snoringNow = result.isSnoring,
                        snoreCount = aggregator.getSnoreCount(),
                        snoreSeconds = aggregator.getTotalSnoreSeconds(),
                        amplitudes = amplitudeWindow.toList()
                    )

                    // Periodic notification refresh every 30 seconds
                    if (timestamp - lastNotificationUpdate >= 30_000L) {
                        lastNotificationUpdate = timestamp
                        val minsElapsed = (elapsed / 60).toInt()
                        val snoreMins = aggregator.getTotalSnoreSeconds() / 60
                        val statusText = if (snoreMins > 0) {
                            "$minsElapsed min elapsed • $snoreMins min snore sound detected"
                        } else {
                            "$minsElapsed min elapsed • Rest is peaceful"
                        }
                        updateNotification("Tracking sleep...", statusText)
                    }
                } else {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in audio tracking loop: ${e.message}", e)
        } finally {
            cleanupAudioResources()
        }
    }

    private fun stopTrackingSession() {
        if (!isRecording) {
            stopSelf()
            return
        }
        isRecording = false
        trackingJob?.cancel()

        serviceScope.launch {
            val endTimestamp = System.currentTimeMillis()
            val finalEvents = aggregator.finishSession(endTimestamp)
            val durationMinutes = max(1, ((endTimestamp - sessionStartTime) / 60_000L).toInt())
            val snoreSeconds = aggregator.getTotalSnoreSeconds()
            val snoreMins = snoreSeconds / 60
            val snoreCount = aggregator.getSnoreCount()

            // Calculate realistic sleep metrics and score
            val durationScore = ((durationMinutes.toFloat() / 480f) * 55f).coerceIn(15f, 55f)
            val snoreRatio = if (durationMinutes > 0) snoreMins.toFloat() / durationMinutes.toFloat() else 0f
            val snoreScore = ((1f - (snoreRatio * 1.8f)) * 45f).coerceIn(10f, 45f)
            val computedScore = (durationScore + snoreScore).toInt().coerceIn(35, 98)

            val restlessMinutes = (snoreMins + (snoreCount * 2)).coerceAtMost(durationMinutes)
            val deepSleepMinutes = ((durationMinutes - restlessMinutes) * 0.45f).toInt().coerceAtLeast(0)
            val lightSleepMinutes = (durationMinutes - deepSleepMinutes - restlessMinutes).coerceAtLeast(0)

            val rating = when {
                snoreMins > 25 || snoreCount > 15 -> "Heavy Snoring Detected"
                snoreMins > 6 || snoreCount > 5 -> "Mild Snoring Detected"
                else -> "Peaceful Rest"
            }

            val sessionEntity = SleepSessionEntity(
                startedAt = sessionStartTime,
                endedAt = endTimestamp,
                durationMinutes = durationMinutes,
                sleepGoalMet = durationMinutes >= 420,
                source = "AUDIO_TRACKING",
                notes = "Estimated sound/snoring insights recorded locally",
                movementCount = snoreCount,
                deepSleepMinutes = deepSleepMinutes,
                lightSleepMinutes = lightSleepMinutes,
                restlessMinutes = restlessMinutes,
                sleepScore = computedScore,
                movementRating = rating,
                snoreEventCount = snoreCount,
                snoreDurationMinutes = snoreMins,
                snorePercentage = if (durationMinutes > 0) (snoreMins * 100 / durationMinutes).coerceIn(0, 100) else 0,
                avgDecibels = aggregator.getAvgDecibels(),
                peakDecibels = aggregator.getPeakDecibels()
            )

            try {
                val db = LuneDatabase.getDatabase(applicationContext)
                val newSessionId = db.sleepSessionDao().insertSleepSession(sessionEntity)

                // Assign session ID to all aggregated snore events and persist to Room
                if (finalEvents.isNotEmpty()) {
                    val eventsWithSessionId = finalEvents.map { it.copy(sessionId = newSessionId) }
                    db.snoreEventDao().insertSnoreEvents(eventsWithSessionId)
                }

                Log.i(TAG, "Successfully persisted sleep session #$newSessionId with ${finalEvents.size} snore events")
                SleepTrackingManager.notifySessionCompleted(newSessionId)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving sleep session to database: ${e.message}", e)
                SleepTrackingManager.updateTrackingState(false)
            } finally {
                cleanupAudioResources()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun cleanupAudioResources() {
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord", e)
        }
        audioRecord = null

        try {
            classifier?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing classifier", e)
        }
        classifier = null

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Sound Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when overnight sound and snore tracking is active"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildTrackingNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SleepSoundTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText("Offline • Private")
            .setSmallIcon(R.drawable.ic_sleep_report_moon)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Tracking",
                stopPendingIntent
            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildTrackingNotification(title, content))
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupAudioResources()
        SleepTrackingManager.updateTrackingState(false)
    }
}
