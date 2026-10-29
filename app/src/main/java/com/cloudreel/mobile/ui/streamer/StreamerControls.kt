package com.cloudreel.mobile.ui.streamer

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.runtime.produceState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WbAuto
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.InfoBlue
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

internal val OverlayColor = Color.Black.copy(alpha = 0.55f)

enum class StreamerTool { EXPOSURE, WHITE_BALANCE, ZOOM }

class StreamerActions(
    val onBack: () -> Unit,
    val onFlip: () -> Unit,
    val onToggleOrientation: () -> Unit,
    val onRecord: () -> Unit,
    val onPause: () -> Unit,
    val onToggleFlash: () -> Unit,
    val onOpenRecordings: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenMetadata: () -> Unit,
    val onToggleFormats: () -> Unit,
    val onSelectFormat: (VideoFormat) -> Unit,
    val onSelectTool: (StreamerTool) -> Unit,
    val onUpdate: ((StreamerUiState) -> StreamerUiState) -> Unit
)

/** All on-screen controls drawn above the camera preview, for portrait and landscape. */
@Composable
fun StreamerControls(
    state: StreamerUiState,
    isLandscape: Boolean,
    isFormatPickerOpen: Boolean,
    selectedTool: StreamerTool?,
    exposureRange: IntRange,
    zoomRange: ClosedFloatingPointRange<Float>,
    actions: StreamerActions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        // ── Top: back + status on the left; format chip (and utilities in landscape) on the right ──
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverlayIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", actions.onBack)
            StatusPill(state)
        }
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormatChip(state.videoFormat, isFormatPickerOpen, isEnabled = !state.isSessionActive, onClick = actions.onToggleFormats)
            if (isLandscape) UtilityButtons(state, actions)
        }

        // ── Left rail: metadata, mic, zoom ────────────────────────────────────
        Column(
            // Landscape is short: pin the rail under the back button so it clears the meter below.
            modifier = if (isLandscape) {
                Modifier.align(Alignment.TopStart).padding(top = 56.dp)
            } else {
                Modifier.align(Alignment.CenterStart)
            },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OverlayIconButton(Icons.Filled.Sell, "Metadata", actions.onOpenMetadata, isActive = state.metadata.isEnabled && state.metadata.hasContent())
            OverlayIconButton(
                icon = if (state.isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                description = if (state.isMicMuted) "Unmute microphone" else "Mute microphone",
                onClick = { actions.onUpdate { it.copy(isMicMuted = !it.isMicMuted) } },
                isActive = state.isMicMuted,
                activeColor = ErrorRed
            )
            ZoomButton(
                zoomRatio = state.zoomRatio,
                isOpen = selectedTool == StreamerTool.ZOOM,
                onClick = { actions.onSelectTool(StreamerTool.ZOOM) }
            )
        }

        // ── Right rail (portrait only): rotate, tools, settings ───────────────
        if (!isLandscape) {
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { UtilityButtons(state, actions) }
        }

        // ── Landscape: the three shutter slots stand in a column on the right ──
        if (isLandscape) {
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { ShutterControls(state, actions) }
        }

        // ── Bottom stack: adjust panel, tools, meter + link status, (portrait) shutter row ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Landscape keeps clear of the shutter column on the right.
                .padding(end = if (isLandscape) 100.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The zoom bar opens from the left rail, the other adjustments from the tools strip.
            val panelTool = selectedTool?.takeIf { it == StreamerTool.ZOOM || state.isToolsOpen }
            if (panelTool != null) {
                AdjustPanel(
                    state = state,
                    tool = panelTool,
                    exposureRange = exposureRange,
                    zoomRange = zoomRange,
                    onUpdate = actions.onUpdate
                )
            }
            if (state.isToolsOpen) ToolsStrip(state, selectedTool, actions)
            // Meter in the bottom-left corner, link status centered, both above the record section.
            Box(modifier = Modifier.fillMaxWidth()) {
                StereoAudioMeter(state.audioLevel, Modifier.align(Alignment.BottomStart))
                LinkStatusPill(state.links, Modifier.align(Alignment.BottomCenter))
            }
            if (!isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) { ShutterControls(state, actions) }
            }
        }

        // Drawn last so the list sits above the right rail; landscape keeps clear of the shutter column.
        if (isFormatPickerOpen) {
            FormatPicker(
                selected = state.videoFormat,
                onSelect = actions.onSelectFormat,
                maxHeight = if (isLandscape) 260.dp else 380.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = if (isLandscape) 92.dp else 0.dp)
            )
        }
    }
}

// ── Pieces ────────────────────────────────────────────────────────────────────

/** Status and timer on top, latency and time left below. The format chip shows the quality. */
@Composable
private fun StatusPill(state: StreamerUiState, modifier: Modifier = Modifier) {
    val (label, color) = when (state.phase) {
        StreamPhase.IDLE -> "READY" to Color.Gray
        StreamPhase.CONNECTING -> "CONNECTING" to WarningAmber
        StreamPhase.LIVE -> "LIVE" to ErrorRed
        StreamPhase.RECORDING -> "REC" to ErrorRed
        StreamPhase.PAUSED -> "PAUSED" to WarningAmber
    }
    val details = "${state.latencySeconds.toString().removeSuffix(".0")}s delay · ${formatTimeLeft(state.timeLeftSeconds)}"
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(OverlayColor)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .semantics { contentDescription = "Status $label, $details" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (state.phase != StreamPhase.IDLE && state.phase != StreamPhase.CONNECTING) {
                    Text(formatTimer(state.elapsedSeconds), color = Color.White, fontSize = 13.sp)
                }
            }
            Text(details, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

/** Opens the zoom bar. Shows the zoom icon at 1×, otherwise the current value. */
@Composable
private fun ZoomButton(zoomRatio: Float, isOpen: Boolean, onClick: () -> Unit) {
    val valueText = String.format(java.util.Locale.US, "%.1f×", zoomRatio)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isOpen) InfoBlue.copy(alpha = 0.85f) else OverlayColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (isOpen) "Close zoom, $valueText" else "Zoom, $valueText" },
        contentAlignment = Alignment.Center
    ) {
        if (abs(zoomRatio - 1f) < 0.05f) {
            Icon(Icons.Filled.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(valueText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 12 segments of 5 dB from -60 dBFS: green up to -20, yellow up to -10, red above.
private const val METER_SEGMENTS = 12
private const val METER_FLOOR_DB = -60f
private const val METER_YELLOW_FROM = 8
private const val METER_RED_FROM = 10
private const val METER_PEAK_HOLD_MS = 1000L

/** Slim stereo L/R level meter with a target mark at -20 dB and a short peak hold. */
private class MeterReading(val decibels: Float, val litSegments: Int, val peakSegments: Int)

/** dBFS, lit segments and the 1 s peak hold for a level; shared by the vertical and horizontal meters. */
@Composable
private fun rememberMeterReading(level: Float): MeterReading {
    // shortcut: CameraX reports one combined mic level, so L and R show the same value;
    // read raw PCM per channel once the live ingest pipeline exists.
    val decibels = if (level > 0f) (20f * log10(level)).coerceIn(METER_FLOOR_DB, 0f) else METER_FLOOR_DB
    val litSegments = ((decibels - METER_FLOOR_DB) / -METER_FLOOR_DB * METER_SEGMENTS).roundToInt()
    val currentLitSegments by rememberUpdatedState(litSegments)
    var peakSegments by remember { mutableIntStateOf(0) }
    LaunchedEffect(litSegments) {
        if (litSegments > peakSegments) peakSegments = litSegments
    }
    LaunchedEffect(peakSegments) {
        delay(METER_PEAK_HOLD_MS)
        peakSegments = currentLitSegments
    }
    return MeterReading(decibels, litSegments, peakSegments)
}

private fun meterZoneColor(index: Int) = when {
    index >= METER_RED_FROM -> ErrorRed
    index >= METER_YELLOW_FROM -> WarningAmber
    else -> SuccessGreen
}

@Composable
private fun StereoAudioMeter(level: Float, modifier: Modifier = Modifier) {
    val reading = rememberMeterReading(level)
    val decibels = reading.decibels
    val litSegments = reading.litSegments
    val peakSegments = reading.peakSegments

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(OverlayColor)
            .padding(6.dp)
            .semantics { contentDescription = "Audio level ${decibels.roundToInt()} dB" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(Modifier.width(22.dp).height(64.dp)) {
            val gap = 1.5.dp.toPx()
            val barWidth = 7.dp.toPx()
            val segmentHeight = (size.height - gap * (METER_SEGMENTS - 1)) / METER_SEGMENTS
            listOf(0f, size.width - barWidth).forEach { barX ->
                for (index in 0 until METER_SEGMENTS) {
                    val zoneColor = meterZoneColor(index)
                    val isLit = index < litSegments || index == peakSegments - 1
                    drawRect(
                        color = if (isLit) zoneColor else zoneColor.copy(alpha = 0.18f),
                        topLeft = Offset(barX, size.height - (index + 1) * segmentHeight - index * gap),
                        size = Size(barWidth, segmentHeight)
                    )
                }
            }
            // Target level mark between the two bars, where yellow starts.
            val targetY = size.height - METER_YELLOW_FROM * (segmentHeight + gap) + gap / 2
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(barWidth + 2.dp.toPx(), targetY),
                end = Offset(size.width - barWidth - 2.dp.toPx(), targetY),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("L", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("R", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Three fixed slots, like the system camera: last recording | record | flip while idle,
 * empty | stop | pause-resume during a session. A row in portrait, a column in landscape.
 */
@Composable
private fun ShutterControls(state: StreamerUiState, actions: StreamerActions) {
    if (state.isSessionActive) {
        // Opening Recordings would leave the camera and end the session, so the slot stays empty.
        Spacer(Modifier.size(52.dp))
    } else {
        LastRecordingButton(state.recordings.firstOrNull(), actions.onOpenRecordings)
    }
    RecordButton(state, actions.onRecord)
    if (state.isSessionActive) {
        val isPaused = state.phase == StreamPhase.PAUSED
        OverlayIconButton(
            icon = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
            description = if (isPaused) "Resume" else "Pause",
            onClick = actions.onPause,
            isEnabled = state.phase != StreamPhase.CONNECTING
        )
    } else {
        OverlayIconButton(Icons.Filled.Cameraswitch, "Switch camera", actions.onFlip)
    }
}

/** Thumbnail of the newest recording; opens the Recordings screen. */
@Composable
private fun LastRecordingButton(latest: RecordingItem?, onClick: () -> Unit) {
    val thumbnail by produceState<ImageBitmap?>(null, latest?.file) {
        value = latest?.file?.let { file -> withContext(Dispatchers.IO) { loadFirstFrame(file) } }
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(shape)
            .background(OverlayColor)
            .border(2.dp, Color.White, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Recordings" },
        contentAlignment = Alignment.Center
    ) {
        val image = thumbnail
        if (image != null) {
            Image(image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

private fun loadFirstFrame(file: File): ImageBitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0)?.asImageBitmap()
    } catch (error: RuntimeException) {
        null // a clip still being written or a damaged file just shows the placeholder
    } finally {
        retriever.release()
    }
}

/** Current format, like the system camera's "1080P / 30" chip. Tap to open the format list. */
@Composable
private fun FormatChip(format: VideoFormat, isOpen: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOpen) InfoBlue.copy(alpha = 0.85f) else OverlayColor)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .semantics { contentDescription = "Video format ${format.displayName}" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(format.label.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            format.rateText,
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White)
                .padding(horizontal = 4.dp)
        )
    }
}

/** Every format as a camera icon with the rate inside (blank = 25/30, A = auto) and the name below. */
@Composable
private fun FormatPicker(
    selected: VideoFormat,
    onSelect: (VideoFormat) -> Unit,
    maxHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(max = maxHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(OverlayColor)
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoFormat.entries.forEach { format ->
            val isSelected = format == selected
            val tint = if (isSelected) InfoBlue else Color.White
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(format) }
                    .padding(vertical = 6.dp)
                    .semantics {
                        contentDescription = format.displayName + if (format.isNative) "" else ", $NOT_ACTIVE_NOTE"
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    Icon(Icons.Outlined.Videocam, contentDescription = null, tint = tint, modifier = Modifier.size(36.dp))
                    // Centered on the body of the camera icon, left of the lens.
                    Text(
                        format.badge,
                        color = tint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 3.dp).width(22.dp)
                    )
                }
                Text(format.label, color = tint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (!format.isNative) {
                    Text("not active", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                }
            }
        }
    }
}

/** Rotate screen, camera tools toggle and settings. */
@Composable
private fun UtilityButtons(state: StreamerUiState, actions: StreamerActions) {
    OverlayIconButton(
        Icons.Filled.ScreenRotation, "Rotate screen", actions.onToggleOrientation,
        isEnabled = !state.isSessionActive
    )
    OverlayIconButton(
        icon = Icons.Filled.Tune,
        description = if (state.isToolsOpen) "Hide camera tools" else "Show camera tools",
        onClick = { actions.onUpdate { it.copy(isToolsOpen = !it.isToolsOpen) } },
        isActive = state.isToolsOpen
    )
    // Settings is a separate screen; leaving the camera ends the session, so it waits until idle.
    OverlayIconButton(Icons.Filled.Settings, "Settings", actions.onOpenSettings, isEnabled = !state.isSessionActive)
}

@Composable
private fun ToolsStrip(state: StreamerUiState, selectedTool: StreamerTool?, actions: StreamerActions) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(OverlayColor)
            .horizontalScroll(rememberScrollState())
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { ToolButtons(state, selectedTool, actions) }
}

@Composable
private fun RecordButton(state: StreamerUiState, onClick: () -> Unit) {
    val label = when {
        state.phase == StreamPhase.CONNECTING -> "Connecting"
        state.isSessionActive -> "Stop"
        state.mode == StreamMode.LIVE -> "Go live"
        else -> "Record"
    }
    val haptics = LocalHapticFeedback.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .clickable(role = Role.Button) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center
        ) {
            when {
                state.phase == StreamPhase.CONNECTING ->
                    CircularProgressIndicator(Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                state.isSessionActive ->
                    Box(Modifier.size(26.dp).clip(RoundedCornerShape(6.dp)).background(ErrorRed))
                else ->
                    Box(Modifier.size(54.dp).clip(CircleShape).background(ErrorRed))
            }
        }
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ToolButtons(state: StreamerUiState, selectedTool: StreamerTool?, actions: StreamerActions) {
    OverlayIconButton(
        icon = if (state.isAutoFocus) Icons.Filled.CenterFocusStrong else Icons.Filled.CenterFocusWeak,
        description = if (state.isAutoFocus) "Autofocus on, tap to lock focus" else "Focus locked, tap for autofocus",
        onClick = { actions.onUpdate { it.copy(isAutoFocus = !it.isAutoFocus) } },
        isActive = state.isAutoFocus
    )
    OverlayIconButton(
        icon = if (state.isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
        description = if (state.isFlashOn) "Turn flash off" else "Turn flash on",
        onClick = actions.onToggleFlash,
        isActive = state.isFlashOn
    )
    OverlayIconButton(
        Icons.Filled.Exposure, "Exposure", { actions.onSelectTool(StreamerTool.EXPOSURE) },
        isActive = selectedTool == StreamerTool.EXPOSURE || state.exposureIndex != 0
    )
    OverlayIconButton(
        Icons.Filled.WbAuto, "White balance", { actions.onSelectTool(StreamerTool.WHITE_BALANCE) },
        isActive = selectedTool == StreamerTool.WHITE_BALANCE || state.whiteBalance != WhiteBalance.AUTO
    )
    OverlayIconButton(
        icon = if (state.isGridOn) Icons.Filled.GridOn else Icons.Filled.GridOff,
        description = if (state.isGridOn) "Hide grid" else "Show grid",
        onClick = { actions.onUpdate { it.copy(isGridOn = !it.isGridOn) } },
        isActive = state.isGridOn
    )
    OverlayIconButton(
        icon = if (state.isExposureLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
        description = if (state.isExposureLocked) "Unlock exposure" else "Lock exposure",
        onClick = { actions.onUpdate { it.copy(isExposureLocked = !it.isExposureLocked) } },
        isActive = state.isExposureLocked
    )
}

@Composable
private fun AdjustPanel(
    state: StreamerUiState,
    tool: StreamerTool,
    exposureRange: IntRange,
    zoomRange: ClosedFloatingPointRange<Float>,
    onUpdate: ((StreamerUiState) -> StreamerUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(OverlayColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (tool) {
            StreamerTool.EXPOSURE -> {
                val canAdjust = exposureRange.first < exposureRange.last
                OverlayIconButton(Icons.Filled.Remove, "Decrease exposure", {
                    onUpdate { it.copy(exposureIndex = (it.exposureIndex - 1).coerceIn(exposureRange.first, exposureRange.last)) }
                }, isEnabled = canAdjust)
                Slider(
                    value = state.exposureIndex.toFloat(),
                    onValueChange = { value -> onUpdate { it.copy(exposureIndex = value.toInt()) } },
                    valueRange = exposureRange.first.toFloat()..maxOf(exposureRange.last, exposureRange.first + 1).toFloat(),
                    enabled = canAdjust,
                    colors = SliderDefaults.colors(thumbColor = InfoBlue, activeTrackColor = InfoBlue),
                    modifier = Modifier.width(160.dp)
                )
                OverlayIconButton(Icons.Filled.Add, "Increase exposure", {
                    onUpdate { it.copy(exposureIndex = (it.exposureIndex + 1).coerceIn(exposureRange.first, exposureRange.last)) }
                }, isEnabled = canAdjust)
                Text(
                    text = if (canAdjust) "EV ${state.exposureIndex}" else "Not supported",
                    color = Color.White, fontSize = 12.sp
                )
            }
            StreamerTool.WHITE_BALANCE -> WhiteBalance.entries.forEach { option ->
                val isSelected = state.whiteBalance == option
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) InfoBlue else Color.Transparent)
                        .clickable(role = Role.RadioButton) { onUpdate { it.copy(whiteBalance = option) } }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(option.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            StreamerTool.ZOOM -> {
                Slider(
                    value = state.zoomRatio.coerceIn(zoomRange),
                    onValueChange = { value ->
                        // Snap to 1× and 2× when the dot is dragged close, so the old presets stay one gesture away.
                        val snapped = listOf(1f, 2f).firstOrNull { it in zoomRange && abs(it - value) < 0.1f } ?: value
                        onUpdate { it.copy(zoomRatio = snapped) }
                    },
                    valueRange = zoomRange.start..maxOf(zoomRange.endInclusive, zoomRange.start + 0.1f),
                    colors = SliderDefaults.colors(thumbColor = InfoBlue, activeTrackColor = InfoBlue),
                    modifier = Modifier.width(200.dp)
                )
                Text(String.format(java.util.Locale.US, "%.1f×", state.zoomRatio), color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LinkStatusPill(links: List<LinkStatus>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(OverlayColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        links.forEach { link ->
            val icon = when (link.kind) {
                LinkKind.WIFI -> Icons.Filled.Wifi
                LinkKind.CELLULAR -> Icons.Filled.CellTower
                LinkKind.INTERNET -> Icons.Filled.Public
                LinkKind.WIRED -> Icons.Filled.Cable
            }
            // Colour is never the only signal: inactive links also read "off".
            val text = when {
                !link.isActive -> "off"
                link.kind == LinkKind.INTERNET -> "ok"
                else -> String.format(java.util.Locale.US, "%.1f", link.megabitsPerSecond)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.semantics { contentDescription = "${link.kind.label} $text" }
            ) {
                Icon(icon, contentDescription = null, tint = if (link.isActive) SuccessGreen else ErrorRed, modifier = Modifier.size(16.dp))
                Text(text, color = Color.White, fontSize = 12.sp)
            }
        }
        Text("Mbps", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
internal fun OverlayIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isEnabled: Boolean = true,
    activeColor: Color = InfoBlue
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor.copy(alpha = 0.85f) else OverlayColor)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (isEnabled) 1f else 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}
