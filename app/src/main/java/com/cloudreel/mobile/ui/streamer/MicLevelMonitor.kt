package com.cloudreel.mobile.ui.streamer

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val SAMPLE_RATE = 44100
private const val REPORT_INTERVAL_MS = 100L

/**
 * Reads the microphone for the level meter until cancelled, reporting the peak of each
 * 100 ms window as linear amplitude (0..1). Not used while CameraX itself records audio.
 */
@SuppressLint("MissingPermission") // callers (camera screen, device setup) only start this once RECORD_AUDIO is granted
suspend fun monitorMicLevel(onLevel: (Float) -> Unit) = withContext(Dispatchers.IO) {
    val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    val recorder = AudioRecord(
        MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
    )
    if (recorder.state != AudioRecord.STATE_INITIALIZED) {
        recorder.release()
        return@withContext
    }
    val buffer = ShortArray(bufferSize / 2)
    var windowPeak = 0
    var windowStart = System.currentTimeMillis()
    try {
        recorder.startRecording()
        while (isActive) {
            val samplesRead = recorder.read(buffer, 0, buffer.size)
            for (index in 0 until samplesRead) windowPeak = maxOf(windowPeak, abs(buffer[index].toInt()))
            if (System.currentTimeMillis() - windowStart >= REPORT_INTERVAL_MS) {
                onLevel(windowPeak / Short.MAX_VALUE.toFloat())
                windowPeak = 0
                windowStart = System.currentTimeMillis()
            }
        }
    } finally {
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop()
        recorder.release()
    }
}
