package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoundManager {
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    fun toggleSound(): Boolean {
        _soundEnabled.value = !_soundEnabled.value
        return _soundEnabled.value
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun playClick() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            generateTone(frequency = 700.0, durationMs = 40, volume = 0.3)
        }
    }

    fun playCorrect() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            // Pleasant ascending chord: C5 -> E5 -> G5
            generateTone(frequency = 523.25, durationMs = 60, volume = 0.4)
            generateTone(frequency = 659.25, durationMs = 60, volume = 0.4)
            generateTone(frequency = 783.99, durationMs = 120, volume = 0.45)
        }
    }

    fun playWrong() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            // Descending buzz
            generateTone(frequency = 220.0, durationMs = 90, volume = 0.35, isSquareWave = true)
            generateTone(frequency = 160.0, durationMs = 140, volume = 0.4, isSquareWave = true)
        }
    }

    fun playLevelUp() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            val notes = listOf(440.0, 554.37, 659.25, 880.0, 1108.73)
            for (note in notes) {
                generateTone(frequency = note, durationMs = 80, volume = 0.45)
            }
        }
    }

    fun playCountdownTick() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            generateTone(frequency = 880.0, durationMs = 30, volume = 0.25)
        }
    }

    fun playGameOver() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            val notes = listOf(587.33, 523.25, 493.88, 440.0, 392.0)
            for (note in notes) {
                generateTone(frequency = note, durationMs = 110, volume = 0.4)
            }
        }
    }

    fun playCombo() {
        if (!_soundEnabled.value) return
        audioScope.launch {
            generateTone(frequency = 987.77, durationMs = 50, volume = 0.3)
            generateTone(frequency = 1318.51, durationMs = 90, volume = 0.4)
        }
    }

    private fun generateTone(
        frequency: Double,
        durationMs: Int,
        volume: Double = 0.5,
        isSquareWave: Boolean = false
    ) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val raw = if (isSquareWave) {
                    if (sin(2.0 * PI * frequency * t) >= 0) 1.0 else -1.0
                } else {
                    sin(2.0 * PI * frequency * t)
                }

                // Envelope attack and release to prevent audio clicking
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                    else -> 1.0
                }

                val value = (raw * envelope * volume * Short.MAX_VALUE).toInt()
                sample[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(sample, 0, numSamples)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Fallback gracefully if audio device is unavailable
        }
    }
}
