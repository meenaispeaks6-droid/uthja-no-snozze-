package com.example.sleep

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class SleepSoundItem(
    val id: String,
    val title: String,
    val tags: String = "",
    val creator: String = "",
    val durationText: String = "29:29",
    val drawableResId: Int? = null,
    val category: String = "All"
)

class SleepAmbientPlayer(private val context: Context) {
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private val _currentSound = MutableStateFlow<SleepSoundItem?>(null)
    val currentSound: StateFlow<SleepSoundItem?> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timerMinutes = MutableStateFlow<Int?>(null) // null = continuous
    val timerMinutes: StateFlow<Int?> = _timerMinutes.asStateFlow()

    fun playSound(item: SleepSoundItem, scope: CoroutineScope) {
        stop()
        if (item.id == "no_sound") {
            _currentSound.value = item
            _isPlaying.value = false
            return
        }

        _currentSound.value = item
        _isPlaying.value = true

        playbackJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBufferSize * 2

            try {
                audioTrack = AudioTrack.Builder()
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

                audioTrack?.play()

                val buffer = ShortArray(1024)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0
                var filterState = 0f

                while (isActive && _isPlaying.value) {
                    when (item.id) {
                        "light_rain" -> {
                            // Pink / filtered noise resembling rain drops on foliage
                            for (i in buffer.indices) {
                                val white = Random.nextFloat() * 2f - 1f
                                filterState = filterState * 0.94f + white * 0.06f
                                val rainDrop = if (Random.nextFloat() < 0.008f) (Random.nextFloat() * 0.4f) else 0f
                                val sample = (filterState * 0.7f + rainDrop) * 8000f
                                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "white_noise" -> {
                            // Soft soothing white noise
                            for (i in buffer.indices) {
                                val white = Random.nextFloat() * 2f - 1f
                                filterState = filterState * 0.85f + white * 0.15f
                                val sample = filterState * 4500f
                                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "lighthouse_keeper" -> {
                            // Warm calming ambient chord harmonic swells (C maj7: C 130.81Hz, E 164.81Hz, G 196Hz, B 246.94Hz)
                            val f1 = 130.81
                            val f2 = 196.00
                            val f3 = 246.94
                            for (i in buffer.indices) {
                                phase1 += 2.0 * Math.PI * f1 / sampleRate
                                phase2 += 2.0 * Math.PI * f2 / sampleRate
                                phase3 += 2.0 * Math.PI * f3 / sampleRate
                                if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                                if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                                if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI

                                val swell = (sin(phase1 * 0.05) + 1.0) * 0.5
                                val wave = (sin(phase1) * 0.5 + sin(phase2) * 0.3 + sin(phase3) * 0.2) * (0.35 + swell * 0.25)
                                buffer[i] = (wave * 9000).toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        "bath_salt" -> {
                            // Effervescent soft bubbling texture
                            for (i in buffer.indices) {
                                val white = Random.nextFloat() * 2f - 1f
                                filterState = filterState * 0.90f + white * 0.10f
                                val fizz = if (Random.nextFloat() < 0.03f) (Random.nextFloat() * 0.5f) else 0f
                                val sample = (filterState * 0.4f + fizz * 0.6f) * 6000f
                                buffer[i] = sample.toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                        else -> { // day1_guide or ambient meditation tone
                            val f = 174.0 // Solfeggio 174Hz calm tone
                            for (i in buffer.indices) {
                                phase1 += 2.0 * Math.PI * f / sampleRate
                                if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                                val wave = sin(phase1) * 0.4
                                buffer[i] = (wave * 7000).toInt().coerceIn(-32767, 32767).toShort()
                            }
                        }
                    }

                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Ignore audio track write exceptions on shutdown
            } finally {
                cleanTrack()
            }
        }
    }

    fun setTimer(minutes: Int?, scope: CoroutineScope) {
        _timerMinutes.value = minutes
        if (minutes != null && minutes > 0) {
            scope.launch {
                delay(minutes * 60 * 1000L)
                if (_timerMinutes.value == minutes) {
                    stop()
                }
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        cleanTrack()
    }

    private fun cleanTrack() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null
    }
}
