package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.DismissType
import com.example.data.model.WakeMode

/**
 * Foreground Service that keeps the device awake, plays continuous alarm audio & vibration,
 * and maintains the persistent full-screen alarm notification over the lock screen until the
 * challenge is completed.
 */
class AlarmRingingService : Service() {

    companion object {
        const val CHANNEL_ID = "lune_smart_alarms"
        const val CHANNEL_NAME = "LUNE Anti-Snooze Alarms"
        const val NOTIFICATION_ID = 9001

        const val ACTION_START_ALARM = "com.aistudio.lune.action.START_ALARM"
        const val ACTION_STOP_ALARM = "com.aistudio.lune.action.STOP_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_WAKE_MODE = "extra_wake_mode"
        const val EXTRA_DISMISS_TYPE = "extra_dismiss_type"
        const val EXTRA_ALARM_SOUND = "extra_alarm_sound"
        const val EXTRA_ALARM_WALLPAPER = "extra_alarm_wallpaper"

        fun start(
            context: Context,
            alarmId: Long,
            title: String,
            wakeMode: String,
            dismissType: String,
            sound: String = "Good Morningggg",
            wallpaper: String = "wp_meow_alarm"
        ) {
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_TITLE, title)
                putExtra(EXTRA_WAKE_MODE, wakeMode)
                putExtra(EXTRA_DISMISS_TYPE, dismissType)
                putExtra(EXTRA_ALARM_SOUND, sound)
                putExtra(EXTRA_ALARM_WALLPAPER, wallpaper)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("AlarmRingingService", "Failed to start foreground service", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("AlarmRingingService", "Failed to stop service via intent", e)
            }
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isRinging = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, 0L) ?: 0L
        val title = intent?.getStringExtra(EXTRA_ALARM_TITLE) ?: "LUNE Wake-Up"
        val wakeMode = intent?.getStringExtra(EXTRA_WAKE_MODE) ?: "FOCUS"
        val dismissType = intent?.getStringExtra(EXTRA_DISMISS_TYPE) ?: "MATH"
        val sound = intent?.getStringExtra(EXTRA_ALARM_SOUND) ?: "Good Morningggg"
        val wallpaper = intent?.getStringExtra(EXTRA_ALARM_WALLPAPER) ?: "wp_meow_alarm"

        startAlarm(alarmId, title, wakeMode, dismissType, sound, wallpaper)

        return START_STICKY
    }

    private fun startAlarm(
        alarmId: Long,
        title: String,
        wakeModeStr: String,
        dismissTypeStr: String,
        sound: String = "Good Morningggg",
        wallpaper: String = "wp_meow_alarm"
    ) {
        if (isRinging) return
        isRinging = true

        // Notify Anti-Cheat manager that an alarm mission is ringing
        com.example.admin.AntiCheatProtectionManager.setAlarmMissionActive(applicationContext, true)

        acquireWakeLock()

        // 1. Create notification channel
        createNotificationChannel()

        // 2. Build Full-Screen Intent directly targeting MainActivity
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("launch_alarm_active", true)
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_TITLE, title)
            putExtra(EXTRA_WAKE_MODE, wakeModeStr)
            putExtra(EXTRA_DISMISS_TYPE, dismissTypeStr)
            putExtra(EXTRA_ALARM_SOUND, sound)
            putExtra(EXTRA_ALARM_WALLPAPER, wallpaper)
        }

        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            pendingFlags
        )

        val challengeDescription = when (dismissTypeStr) {
            "SHAKE" -> "Shake Phone Challenge Active!"
            "MATH" -> "Cognitive Math Challenge Active!"
            "BOTH" -> "Math + Shake Challenges Active!"
            else -> "Awaken & Complete Challenge!"
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $title")
            .setContentText("$challengeDescription Cannot close until completed.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // 3. Play alarm audio in a loop
        startAudioPlayback(sound)

        // 4. Start pulsing vibration in a loop
        startVibration()

        // 5. Explicitly launch MainActivity so it opens over the lock screen immediately
        try {
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to launch MainActivity directly", e)
        }
    }

    private fun startAudioPlayback(sound: String? = null) {
        try {
            val ringtoneItem = sound?.let { com.example.data.model.RingtoneCatalog.findByNameOrId(it) }
            if (ringtoneItem?.rawResId != null) {
                mediaPlayer = MediaPlayer.create(applicationContext, ringtoneItem.rawResId).apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    start()
                }
            } else {
                val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, alertUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error starting MediaPlayer", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 700, 300, 700, 300, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error starting vibrator", e)
        }
    }

    private fun stopAlarm() {
        isRinging = false
        com.example.admin.AntiCheatProtectionManager.setAlarmMissionActive(applicationContext, false)
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error releasing MediaPlayer", e)
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error stopping vibrator", e)
        }

        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Lune:AlarmRingingServiceWakeLock"
            )?.apply {
                acquire(10 * 60 * 1000L) // 10 minutes max safety limit
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error releasing wake lock", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarms that display over the lock screen requiring challenge dismissal."
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(alarmUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
