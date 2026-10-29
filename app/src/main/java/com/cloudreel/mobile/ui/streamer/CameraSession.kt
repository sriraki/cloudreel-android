package com.cloudreel.mobile.ui.streamer

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.util.Range
import android.view.Surface
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.TimeUnit

/** Thin wrapper over CameraX: one preview, one video recorder, and the manual controls. */
@OptIn(ExperimentalCamera2Interop::class)
class CameraSession(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: Camera? = null

    val hasFlashUnit: Boolean
        get() = camera?.cameraInfo?.hasFlashUnit() == true

    val maxZoomRatio: Float
        get() = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f

    val minZoomRatio: Float
        get() = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f

    val exposureRange: IntRange
        get() = camera?.cameraInfo?.exposureState?.exposureCompensationRange
            ?.let { it.lower..it.upper } ?: 0..0

    fun bind(
        owner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        format: VideoFormat,
        onBound: () -> Unit,
        onMessage: (String) -> Unit
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider

                val selector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                val quality = when (format.captureHeight) {
                    1080 -> Quality.FHD
                    720 -> Quality.HD
                    else -> Quality.SD
                }
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(quality, FallbackStrategy.lowerQualityOrHigherThan(quality))
                    )
                    .build()
                // 50 and 60 fps depend on the phone; without a matching range the camera picks its own rate.
                val supportedRanges = provider.getCameraInfo(selector).supportedFrameRateRanges
                val frameRate = format.captureFps?.takeIf { fps -> supportedRanges.any { it.upper == fps } }
                if (format.captureFps != null && frameRate == null) {
                    onMessage("${format.captureFps} fps is not supported on this phone, using auto frame rate")
                }
                val capture = VideoCapture.Builder(recorder)
                    .apply { if (frameRate != null) setTargetFrameRate(Range(frameRate, frameRate)) }
                    .build()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                provider.unbindAll()
                camera = provider.bindToLifecycle(owner, selector, preview, capture)
                previewUseCase = preview
                videoCapture = capture
                updateRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                onBound()
            } catch (error: Exception) {
                onMessage(error.localizedMessage ?: "Camera is not available")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun updateRotation(rotation: Int) {
        previewUseCase?.targetRotation = rotation
        videoCapture?.targetRotation = rotation
    }

    fun release() {
        recording?.stop()
        recording = null
        // Only this session's use cases: the screen being returned to may already have bound its own preview.
        cameraProvider?.unbind(*listOfNotNull(previewUseCase, videoCapture).toTypedArray())
    }

    // ── Manual controls ───────────────────────────────────────────────────────

    fun setTorch(isOn: Boolean) {
        camera?.cameraControl?.enableTorch(isOn)
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio.coerceIn(minZoomRatio, maxZoomRatio))
    }

    fun setExposureIndex(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index.coerceIn(exposureRange.first, exposureRange.last))
    }

    /** White balance and AE lock share one request-options call, a second call would overwrite the first. */
    fun setCaptureOptions(whiteBalance: WhiteBalance, isExposureLocked: Boolean) {
        val control = camera?.cameraControl ?: return
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, whiteBalance.awbMode)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, isExposureLocked)
            .build()
        Camera2CameraControl.from(control).setCaptureRequestOptions(options)
    }

    /** With autofocus on the lens refocuses by itself after 5s; off keeps the tapped focus locked. */
    fun focusAt(previewView: PreviewView, x: Float, y: Float, isAutoFocus: Boolean) {
        val point = previewView.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).apply {
            if (isAutoFocus) setAutoCancelDuration(5, TimeUnit.SECONDS) else disableAutoCancel()
        }.build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    // ── Recording ─────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission") // the screen only shows the camera after the permission is granted
    fun startRecording(file: File, isMuted: Boolean, onEvent: (VideoRecordEvent) -> Unit) {
        val capture = videoCapture ?: return
        recording = capture.output
            .prepareRecording(context, FileOutputOptions.Builder(file).build())
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) recording = null
                onEvent(event)
            }
        recording?.mute(isMuted)
    }

    fun pauseRecording() = recording?.pause()

    fun resumeRecording() = recording?.resume()

    fun stopRecording() {
        recording?.stop()
    }

    fun setMuted(isMuted: Boolean) {
        recording?.mute(isMuted)
    }
}
