package com.cloudreel.mobile.ui.streamer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.ui.components.GradientButton
import com.cloudreel.mobile.ui.components.rememberCameraPermission
import com.cloudreel.mobile.ui.components.SecondaryButton
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@Composable
fun StreamerScreen(
    viewModel: StreamerViewModel,
    onNavigateBack: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenSettings: () -> Unit,
    role: StreamerRole = StreamerRole.REPORTER,
    sessionCode: String = "",
    onLeaveMeeting: (hasEnded: Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val cameraPermission = rememberCameraPermission()
    val isMeeting = role != StreamerRole.REPORTER

    LaunchedEffect(role) {
        when (role) {
            StreamerRole.HOST -> viewModel.startHostSession(sessionCode)
            StreamerRole.GUEST -> viewModel.startGuestSession(sessionCode)
            StreamerRole.REPORTER -> Unit
        }
    }

    // The camera screen follows Settings > Advanced > Preview orientation; the rest of the app keeps its default.
    // A meeting has no rotate button, so its screen always follows the phone.
    val previewOrientation = viewModel.uiState.collectAsState().value.settings.previewOrientation
    DisposableEffect(previewOrientation, isMeeting) {
        val activity = context.findActivity()
        activity?.requestedOrientation = if (isMeeting) ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR else when (previewOrientation) {
            PreviewOrientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            PreviewOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            PreviewOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    if (cameraPermission.isGranted) {
        CameraContent(viewModel, onNavigateBack, onOpenRecordings, onOpenSettings, isMeeting, onLeaveMeeting)
    } else {
        PermissionGate(
            wasDenied = cameraPermission.wasDenied,
            onRequest = cameraPermission.request,
            onOpenAppSettings = cameraPermission.openAppSettings,
            onBack = onNavigateBack
        )
    }
}

@Composable
private fun CameraContent(
    viewModel: StreamerViewModel,
    onNavigateBack: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenSettings: () -> Unit,
    isMeeting: Boolean,
    onLeaveMeeting: (hasEnded: Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val cameraSession = remember { CameraSession(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // A gallery tile must clip the preview; the default SurfaceView mode draws past its bounds.
            if (isMeeting) implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val isAutoFocus = rememberUpdatedState(state.isAutoFocus)
    var boundVersion by remember { mutableIntStateOf(0) }
    var selectedTool by remember { mutableStateOf<StreamerTool?>(null) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var isMetadataOpen by remember { mutableStateOf(false) }
    var isFormatPickerOpen by remember { mutableStateOf(false) }
    var isStopConfirmOpen by remember { mutableStateOf(false) }
    // Go live waits for the metadata popup when Settings asks for it.
    var isGoLivePending by remember { mutableStateOf(false) }
    // Double tap on the preview hides or shows every overlay for a clean frame.
    var isOverlayVisible by remember { mutableStateOf(true) }
    var participantsTab by remember { mutableStateOf<ParticipantsTab?>(null) }
    var guestActionsFor by remember { mutableStateOf<Guest?>(null) }
    // Meeting: the bars step out of the way after 5 seconds, as in Teams or Zoom; a tap brings them back.
    LaunchedEffect(isOverlayVisible) {
        if (isMeeting && isOverlayVisible) {
            delay(MEETING_BARS_HIDE_MS)
            isOverlayVisible = false
        }
    }
    var isLeaveSheetOpen by remember { mutableStateOf(false) }
    val isScreenSharing by ScreenShareService.isSharing.collectAsState()
    val screenCaptureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val permissionData = result.data
        if (result.resultCode == Activity.RESULT_OK && permissionData != null) ScreenShareService.start(context, result.resultCode, permissionData)
    }
    fun onToggleScreenShare() {
        if (isScreenSharing) {
            ScreenShareService.stop(context)
        } else {
            screenCaptureLauncher.launch(context.getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent())
        }
    }
    // The call clock ticks from when the session started, so it survives opening the people page.
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isMeeting) {
        while (isMeeting) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }
    val isDoubleTapEnabled = state.settings.isDoubleTapHidesControls
    LaunchedEffect(isDoubleTapEnabled) {
        if (!isDoubleTapEnabled) isOverlayVisible = true
    }
    // Mirroring flips the on-screen preview only, not what is streamed.
    LaunchedEffect(state.settings.previewMirroring) {
        previewView.scaleX = if (state.settings.previewMirroring == PreviewMirroring.HORIZONTAL) -1f else 1f
        previewView.scaleY = if (state.settings.previewMirroring == PreviewMirroring.VERTICAL) -1f else 1f
    }

    // ── Camera lifecycle ──────────────────────────────────────────────────────
    LaunchedEffect(state.isFrontCamera, state.videoFormat) {
        cameraSession.bind(
            owner = lifecycleOwner,
            previewView = previewView,
            isFrontCamera = state.isFrontCamera,
            format = state.videoFormat,
            onBound = { boundVersion++ },
            onMessage = { viewModel.showMessage(it) }
        )
    }
    DisposableEffect(Unit) {
        previewView.keepScreenOn = true
        onDispose {
            previewView.keepScreenOn = false
            cameraSession.release()
            viewModel.stopSession()
            if (isMeeting) ScreenShareService.stop(context)
        }
    }
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration.orientation) {
        previewView.display?.rotation?.let { cameraSession.updateRotation(it) }
    }

    // ── Push UI state into the camera ─────────────────────────────────────────
    LaunchedEffect(boundVersion, state.isFlashOn) { cameraSession.setTorch(state.isFlashOn) }
    LaunchedEffect(boundVersion, state.zoomRatio) { cameraSession.setZoomRatio(state.zoomRatio) }
    LaunchedEffect(boundVersion, state.exposureIndex) { cameraSession.setExposureIndex(state.exposureIndex) }
    LaunchedEffect(boundVersion, state.whiteBalance, state.isExposureLocked) {
        cameraSession.setCaptureOptions(state.whiteBalance, state.isExposureLocked)
    }
    LaunchedEffect(state.isMicMuted) { cameraSession.setMuted(state.isMicMuted) }

    // The meter always shows the mic, idle or live. While CameraX records a file it owns the
    // mic and its recording stats feed the meter instead.
    val isCameraRecordingAudio = state.mode == StreamMode.STORE && state.isSessionActive
    LaunchedEffect(state.isMicMuted, isCameraRecordingAudio) {
        if (state.isMicMuted) {
            viewModel.update { it.copy(audioLevel = 0f) }
        } else if (!isCameraRecordingAudio) {
            monitorMicLevel { level -> viewModel.update { it.copy(audioLevel = level) } }
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    // The zoom bar closes itself 3 seconds after the last zoom change.
    LaunchedEffect(selectedTool, state.zoomRatio) {
        if (selectedTool == StreamerTool.ZOOM) {
            delay(3000)
            selectedTool = null
        }
    }
    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            delay(1200)
            focusPoint = null
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    fun startStoreRecording() {
        cameraSession.startRecording(viewModel.newRecordingFile(), state.isMicMuted) { event ->
            when (event) {
                is VideoRecordEvent.Start -> viewModel.onRecordingStarted()
                is VideoRecordEvent.Status -> {
                    val amplitude = event.recordingStats.audioStats.audioAmplitude.toFloat()
                    viewModel.update { it.copy(audioLevel = if (amplitude > 1f) amplitude / 32767f else amplitude) }
                }
                is VideoRecordEvent.Finalize -> viewModel.onRecordingFinished(
                    if (event.hasError()) "Recording failed (code ${event.error})" else null
                )
                else -> Unit
            }
        }
    }

    fun onRecordClick() {
        when {
            state.phase == StreamPhase.CONNECTING -> Unit
            state.isSessionActive && state.mode == StreamMode.LIVE -> isStopConfirmOpen = true
            state.isSessionActive -> cameraSession.stopRecording()
            state.mode == StreamMode.LIVE && state.metadata.isPromptOnStart -> {
                isGoLivePending = true
                isMetadataOpen = true
            }
            state.mode == StreamMode.LIVE -> viewModel.startLive()
            else -> startStoreRecording()
        }
    }

    // Out of quota: end whatever is running.
    LaunchedEffect(state.timeLeftSeconds, state.phase) {
        if (state.timeLeftSeconds == 0 && state.isSessionActive) {
            if (state.mode == StreamMode.LIVE) viewModel.stopSession() else cameraSession.stopRecording()
            viewModel.showMessage("Time limit reached")
        }
    }

    // Leaving a meeting is always confirmed, because the others are still in it.
    fun onBackClick() {
        when {
            state.isMeeting -> isLeaveSheetOpen = true
            state.isSessionActive -> onRecordClick()
            else -> onNavigateBack()
        }
    }

    BackHandler(enabled = state.isMeeting || state.isSessionActive) { onBackClick() }

    val actions = StreamerActions(
        onBack = ::onBackClick,
        onFlip = {
            viewModel.update {
                it.copy(isFrontCamera = !it.isFrontCamera, isFlashOn = false, zoomRatio = 1f, exposureIndex = 0)
            }
        },
        // Rotate locks to the other orientation; Settings > Advanced > Preview orientation returns to Auto.
        onToggleOrientation = {
            viewModel.update {
                it.copy(
                    settings = it.settings.copy(
                        previewOrientation = if (isLandscape) PreviewOrientation.PORTRAIT else PreviewOrientation.LANDSCAPE
                    )
                )
            }
        },
        onRecord = ::onRecordClick,
        onPause = {
            if (state.phase == StreamPhase.PAUSED) {
                cameraSession.resumeRecording()
                viewModel.setPaused(false)
            } else {
                cameraSession.pauseRecording()
                viewModel.setPaused(true)
            }
        },
        onToggleFlash = {
            if (cameraSession.hasFlashUnit) {
                viewModel.update { it.copy(isFlashOn = !it.isFlashOn) }
            } else {
                viewModel.showMessage("Flash is not available on this camera")
            }
        },
        onOpenRecordings = onOpenRecordings,
        onOpenSettings = onOpenSettings,
        onOpenMetadata = { isMetadataOpen = true },
        onToggleFormats = { isFormatPickerOpen = !isFormatPickerOpen },
        onSelectFormat = { format ->
            isFormatPickerOpen = false
            viewModel.update { it.copy(videoFormat = format) }
            if (!format.isNative) viewModel.showMessage("${format.displayName} saved. $NOT_ACTIVE_NOTE, the camera records progressive video.")
        },
        onSelectTool = { tool -> selectedTool = if (selectedTool == tool) null else tool },
        onUpdate = viewModel::update
    )

    val toggleOverlays: (() -> Unit)? = if (isDoubleTapEnabled) {
        { isOverlayVisible = !isOverlayVisible }
    } else {
        null
    }
    // The camera preview; the host's program frame places it in a tile, everyone else sees it full screen.
    val cameraPreview: @Composable (Modifier) -> Unit = { placement ->
        AndroidView(
            factory = { previewView },
            modifier = placement
                .pointerInput(isDoubleTapEnabled) {
                    detectTapGestures(
                        onDoubleTap = toggleOverlays?.let { { _ -> it() } },
                        onTap = { tap ->
                            focusPoint = tap
                            cameraSession.focusAt(previewView, tap.x, tap.y, isAutoFocus.value)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        viewModel.update {
                            it.copy(zoomRatio = (it.zoomRatio * zoom).coerceIn(cameraSession.minZoomRatio, cameraSession.maxZoomRatio))
                        }
                    }
                }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // The role never changes on this screen, so the preview view always stays in the same branch.
        if (isMeeting) {
            // Meeting view: tap the video to show or hide the bars; the host taps a guest for their actions.
            MeetingGallery(
                state = state,
                isLandscape = isLandscape,
                isSelfSpeaking = !state.isMicMuted && state.audioLevel > SPEAKING_LEVEL,
                isScreenSharing = isScreenSharing,
                // While the bars are hidden, the first tap anywhere only brings them back.
                onGuestTap = if (state.isHostSession) {
                    { guest -> if (isOverlayVisible) guestActionsFor = guest else isOverlayVisible = true }
                } else {
                    null
                },
                onBackgroundTap = { isOverlayVisible = !isOverlayVisible },
                selfPreview = { placement ->
                    AndroidView(
                        factory = { previewView },
                        modifier = placement.pointerInput(Unit) { detectTapGestures(onTap = { isOverlayVisible = !isOverlayVisible }) }
                    )
                }
            )
            MeetingBars(
                state = state,
                isVisible = isOverlayVisible,
                callSeconds = ((nowMillis - state.callStartedAtMillis) / 1000).toInt().coerceAtLeast(0),
                isScreenSharing = isScreenSharing,
                onFlip = actions.onFlip,
                onOpenPeople = { tab -> participantsTab = tab },
                onToggleVideo = { viewModel.update { it.copy(isCameraOff = !it.isCameraOff) } },
                onToggleMic = { viewModel.update { it.copy(isMicMuted = !it.isMicMuted) } },
                onToggleScreenShare = ::onToggleScreenShare,
                onRecord = { viewModel.showMessage("Session recording: $NOT_ACTIVE_NOTE") },
                onLeave = { isLeaveSheetOpen = true },
                onAdmit = viewModel::admitGuest,
                onDeny = viewModel::denyGuest
            )
            participantsTab?.let { tab ->
                ParticipantsPage(state = state, initialTab = tab, viewModel = viewModel, onClose = { participantsTab = null })
            }
        } else {
            cameraPreview(Modifier.fillMaxSize())

            if (state.isGridOn && isOverlayVisible) GridOverlay()

            focusPoint?.let { point ->
                Box(
                    Modifier
                        .offset { IntOffset(point.x.roundToInt() - 36.dp.roundToPx(), point.y.roundToInt() - 36.dp.roundToPx()) }
                        .size(72.dp)
                        .border(2.dp, CRMagenta, CircleShape)
                )
            }

            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                StreamerControls(
                    state = state,
                    isLandscape = isLandscape,
                    isFormatPickerOpen = isFormatPickerOpen && !state.isSessionActive,
                    selectedTool = selectedTool,
                    exposureRange = remember(boundVersion) { cameraSession.exposureRange },
                    zoomRange = remember(boundVersion) { cameraSession.minZoomRatio..cameraSession.maxZoomRatio },
                    actions = actions
                )
            }
        }
    }

    if (isMetadataOpen) {
        MetadataDialog(
            metadata = state.metadata,
            onSave = { updated -> viewModel.update { it.copy(metadata = updated) } },
            onDismiss = {
                isMetadataOpen = false
                isGoLivePending = false
            },
            isGoLive = isGoLivePending,
            onGoLive = {
                isMetadataOpen = false
                isGoLivePending = false
                viewModel.startLive()
            }
        )
    }
    guestActionsFor?.let { tapped ->
        // Read the latest copy so mute reflects changes made while the sheet is open.
        state.guests.find { it.id == tapped.id }?.let { guest ->
            GuestActionsSheet(guest = guest, viewModel = viewModel, onDismiss = { guestActionsFor = null })
        }
    }
    if (isLeaveSheetOpen) {
        LeaveSessionSheet(
            isHost = state.isHostSession,
            onLeave = {
                isLeaveSheetOpen = false
                onLeaveMeeting(false)
            },
            onEndForEveryone = {
                isLeaveSheetOpen = false
                onLeaveMeeting(true)
            },
            onStay = { isLeaveSheetOpen = false }
        )
    }
    if (isStopConfirmOpen) {
        StopBroadcastDialog(
            onConfirm = {
                isStopConfirmOpen = false
                viewModel.stopSession()
            },
            onDismiss = { isStopConfirmOpen = false }
        )
    }
}

@Composable
private fun GridOverlay() {
    Canvas(Modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.35f)
        for (index in 1..2) {
            val x = size.width * index / 3f
            val y = size.height * index / 3f
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
        }
    }
}

@Composable
private fun PermissionGate(
    wasDenied: Boolean,
    onRequest: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera and microphone access",
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "CloudReel needs your camera and microphone to go live or record. Video stays on this phone until you choose to stream or upload it.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        GradientButton(text = "Allow access", onClick = onRequest)
        if (wasDenied) {
            SecondaryButton(text = "Open app settings", onClick = onOpenAppSettings, modifier = Modifier.padding(top = 12.dp))
        }
        SecondaryButton(text = "Back", onClick = onBack, modifier = Modifier.padding(top = 12.dp))
    }
}

/** Mic level (0..1) above which the host's tile shows the speaking ring; about -26 dBFS, normal speech. */
private const val SPEAKING_LEVEL = 0.05f
private const val MEETING_BARS_HIDE_MS = 5000L

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
