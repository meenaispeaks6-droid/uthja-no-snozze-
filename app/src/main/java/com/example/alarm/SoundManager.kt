package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.R
import com.example.data.model.RingtoneCatalog
import com.example.data.model.RingtoneItem
import com.example.data.model.WakeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private var soundJob: Job? = null
    private var isPlaying = false

    private var vibeJob: Job? = null
    private var currentPlayingVibe: String? = null
    private var previewMediaPlayer: MediaPlayer? = null
    private var alarmMediaPlayer: MediaPlayer? = null

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    try {
                        val hindi = Locale("hi", "IN")
                        val avail = textToSpeech?.isLanguageAvailable(hindi) ?: TextToSpeech.LANG_NOT_SUPPORTED
                        if (avail >= TextToSpeech.LANG_AVAILABLE) {
                            textToSpeech?.language = hindi
                        } else {
                            textToSpeech?.language = Locale.ENGLISH
                        }
                        textToSpeech?.setPitch(1.15f)
                        textToSpeech?.setSpeechRate(1.10f)
                    } catch (e: Exception) {
                        Log.e("SoundManager", "Error setting TTS language", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to init TextToSpeech", e)
        }
    }

    fun isVibePlaying(vibeName: String): Boolean {
        return currentPlayingVibe == vibeName && (vibeJob?.isActive == true || previewMediaPlayer?.isPlaying == true)
    }

    fun stopVibePreview() {
        vibeJob?.cancel()
        vibeJob = null
        currentPlayingVibe = null

        try {
            previewMediaPlayer?.stop()
            previewMediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        previewMediaPlayer = null

        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playVibePreview(vibeName: String, scope: CoroutineScope, onStop: () -> Unit = {}) {
        val item = RingtoneCatalog.findByNameOrId(vibeName)
        playRingtonePreview(item, scope, onStop)
    }

    fun playRingtonePreview(item: RingtoneItem, scope: CoroutineScope, onStop: () -> Unit = {}) {
        stopVibePreview()
        stopAlarmTone()
        currentPlayingVibe = item.name

        // 1. If it's a raw MP3 (like our 👾 Meme ringtones)
        if (item.rawResId != null) {
            vibeJob = scope.launch(Dispatchers.IO) {
                try {
                    previewMediaPlayer = MediaPlayer.create(context, item.rawResId).apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        start()
                    }

                    // Optional TTS speech accompaniment
                    if (isTtsReady && !item.speechText.isNullOrBlank()) {
                        try {
                            textToSpeech?.speak(
                                item.speechText,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "meme_preview_${item.id}"
                            )
                        } catch (e: Exception) {
                            Log.e("SoundManager", "TTS speech failed", e)
                        }
                    }

                    // Auto-stop preview after 25s
                    delay(25_000L)
                } catch (e: Exception) {
                    Log.e("SoundManager", "Failed to play raw media preview", e)
                } finally {
                    if (currentPlayingVibe == item.name) {
                        currentPlayingVibe = null
                        stopVibePreview()
                        onStop()
                    }
                }
            }
            return
        }

        // 2. Synthesized melodic / nature note progression
        vibeJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                null
            }

            if (audioTrack == null) {
                playSystemFallback()
                return@launch
            }

            try {
                audioTrack.play()
            } catch (e: Exception) {
                return@launch
            }

            // Note sequences for vibes
            val (frequencies, noteDuration, pauseBetween) = when (item.name.trim()) {
                "Good Morningggg" -> Triple(listOf(523.25, 659.25, 783.99, 880.00, 1046.50), 0.28, 400L)
                "Time for School" -> Triple(listOf(659.25, 830.61, 987.77, 1318.51, 987.77), 0.20, 250L)
                "Happy Morning" -> Triple(listOf(698.46, 880.00, 1046.50, 1174.66, 1046.50, 880.00), 0.22, 350L)
                "Angelic Wake Up" -> Triple(listOf(587.33, 739.99, 880.00, 1108.73, 1479.98), 0.45, 600L)
                "Wake up you lazy" -> Triple(listOf(783.99, 1046.50, 783.99, 1046.50, 1318.51), 0.16, 180L)
                "Rain" -> Triple(listOf(220.0, 330.0, 440.0, 293.66), 0.6, 100L)
                "Ocean" -> Triple(listOf(130.81, 196.00, 261.63, 196.00), 0.8, 200L)
                "Forest" -> Triple(listOf(1174.66, 1396.91, 1760.00, 1396.91), 0.25, 450L)
                "Birds" -> Triple(listOf(1760.00, 2093.00, 2349.32, 2637.02), 0.15, 300L)
                "Wind" -> Triple(listOf(164.81, 220.00, 196.00, 164.81), 0.7, 150L)
                else -> Triple(listOf(523.25, 659.25, 783.99, 1046.50), 0.30, 400L)
            }

            try {
                // Play 4 loops for preview
                for (loop in 0 until 4) {
                    if (!isActive || currentPlayingVibe != item.name) break
                    for (freq in frequencies) {
                        if (!isActive || currentPlayingVibe != item.name) break
                        val numSamples = (sampleRate * noteDuration).toInt()
                        val buffer = ShortArray(numSamples)

                        for (i in 0 until numSamples) {
                            val t = i.toDouble() / sampleRate
                            val envelope = if (i < numSamples * 0.12) {
                                i / (numSamples * 0.12)
                            } else if (i > numSamples * 0.75) {
                                (numSamples - i) / (numSamples * 0.25)
                            } else 1.0

                            val wave = sin(2.0 * Math.PI * freq * t) + 0.25 * sin(4.0 * Math.PI * freq * t)
                            val sampleValue = (wave * envelope * 0.5 * Short.MAX_VALUE).toInt()
                            buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                        audioTrack.write(buffer, 0, buffer.size)
                    }
                    delay(pauseBetween)
                }
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
                if (currentPlayingVibe == item.name) {
                    currentPlayingVibe = null
                    onStop()
                }
            }
        }
    }

    fun playAlarmTone(mode: WakeMode, scope: CoroutineScope) {
        playAlarmToneWithSound(null, mode, scope)
    }

    fun playAlarmToneWithSound(soundName: String?, mode: WakeMode, scope: CoroutineScope) {
        stopAlarmTone()
        stopVibePreview()
        isPlaying = true

        val ringtone = soundName?.let { RingtoneCatalog.findByNameOrId(it) }

        // If it's a meme sound with raw MP3, play with MediaPlayer
        if (ringtone?.rawResId != null) {
            soundJob = scope.launch(Dispatchers.IO) {
                try {
                    alarmMediaPlayer = MediaPlayer.create(context, ringtone.rawResId).apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = true
                        start()
                    }

                    // Loop spoken lines for extra meme energy
                    while (isActive && isPlaying) {
                        if (isTtsReady && !ringtone.speechText.isNullOrBlank()) {
                            textToSpeech?.speak(
                                ringtone.speechText,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "alarm_meme_speech"
                            )
                        }
                        delay(22_000L)
                    }
                } catch (e: Exception) {
                    Log.e("SoundManager", "Error playing meme alarm", e)
                    playSystemFallback()
                }
            }
            return
        }

        // Standard synthesizer fallback according to WakeMode
        soundJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                Log.e("SoundManager", "Failed to build AudioTrack, falling back to system ringtone", e)
                null
            }

            if (audioTrack == null) {
                playSystemFallback()
                return@launch
            }

            try {
                audioTrack.play()
            } catch (e: Exception) {
                Log.e("SoundManager", "Error playing AudioTrack", e)
                return@launch
            }

            val frequencies = when (mode) {
                WakeMode.GENTLE -> listOf(523.25, 659.25, 783.99, 1046.50)
                WakeMode.FOCUS -> listOf(587.33, 880.00, 587.33, 1174.66)
                WakeMode.BEAST -> listOf(880.00, 1760.00, 987.77, 1975.53)
            }

            val noteDurationSeconds = when (mode) {
                WakeMode.GENTLE -> 0.35
                WakeMode.FOCUS -> 0.22
                WakeMode.BEAST -> 0.12
            }

            val pauseBetweenSequences = when (mode) {
                WakeMode.GENTLE -> 800L
                WakeMode.FOCUS -> 400L
                WakeMode.BEAST -> 100L
            }

            while (isActive && isPlaying) {
                for (freq in frequencies) {
                    if (!isActive || !isPlaying) break
                    val numSamples = (sampleRate * noteDurationSeconds).toInt()
                    val buffer = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = if (i < numSamples * 0.1) {
                            i / (numSamples * 0.1)
                        } else if (i > numSamples * 0.8) {
                            (numSamples - i) / (numSamples * 0.2)
                        } else 1.0

                        val sampleValue = (sin(2.0 * Math.PI * freq * t) * envelope * 0.7 * Short.MAX_VALUE).toInt()
                        buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
                delay(pauseBetweenSequences)
            }

            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun playSystemFallback() {
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alert)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("SoundManager", "Fallback ringtone failed", e)
        }
    }

    fun stopAlarmTone() {
        isPlaying = false
        soundJob?.cancel()
        soundJob = null

        try {
            alarmMediaPlayer?.stop()
            alarmMediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        alarmMediaPlayer = null

        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
