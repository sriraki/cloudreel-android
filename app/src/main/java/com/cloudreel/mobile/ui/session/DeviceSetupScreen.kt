package com.cloudreel.mobile.ui.session

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.media.ToneGenerator
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.theme.WarningAmber
import com.cloudreel.mobile.ui.components.BottomActionBar
import com.cloudreel.mobile.ui.components.CameraPermissionState
import com.cloudreel.mobile.ui.components.CloudReelTopBar
import com.cloudreel.mobile.ui.components.GradientButton
import com.cloudreel.mobile.ui.components.ListDivider
import com.cloudreel.mobile.ui.components.ListGroup
import com.cloudreel.mobile.ui.components.ListRow
import com.cloudreel.mobile.ui.components.rememberCameraPermission
import com.cloudreel.mobile.ui.streamer.monitorMicLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun DeviceSetupScreen(
    mode: String,
    sessionCode: String,
    onNavigateBack: () -> Unit,
    onStartSession: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cameraPermission = rememberCameraPermission()

    var isMicEnabled by remember { mutableStateOf(true) }
    var isCameraEnabled by remember { mutableStateOf(true) }
    var selectedSpeaker by remember { mutableStateOf("Speaker") }
    var isSpeakerMenuExpanded by remember { mutableStateOf(false) }
    var selectedCamera by remember { mutableStateOf("Front Camera") }
    var isCameraMenuExpanded by remember { mutableStateOf(false) }
    var isTestingSpeaker by remember { mutableStateOf(false) }
    var isSpeakerMissing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isHost = mode == "host"
    var micLevel by remember { mutableFloatStateOf(0f) }

    // Listen to the mic only while this screen shows it as on; leaving the screen cancels this and frees the mic.
    LaunchedEffect(isMicEnabled, cameraPermission.isGranted) {
        micLevel = 0f
        if (isMicEnabled && cameraPermission.isGranted) monitorMicLevel { micLevel = it }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CloudReelTopBar(
                title = stringResource(R.string.device_setup_title),
                onBackClick = onNavigateBack
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── CAMERA PREVIEW VIEWPORT ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF16161D))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                ) {
                    if (!cameraPermission.isGranted) {
                        CameraAccessPrompt(cameraPermission)
                    } else if (isCameraEnabled) {
                        LiveCameraPreview(isFrontCamera = selectedCamera == "Front Camera")
                    } else {
                        // Camera Off Placeholder
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C38)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Camera is turned off",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Bottom-left: "You" Badge Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "You",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Bottom-right: Mic & Video Toggle Action Buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mic Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMicEnabled) Color.Black.copy(alpha = 0.55f)
                                    else ErrorRed.copy(alpha = 0.85f)
                                )
                                .clickable { isMicEnabled = !isMicEnabled }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isMicEnabled) "Mute microphone" else "Unmute microphone"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Video Camera Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCameraEnabled) Color.Black.copy(alpha = 0.55f)
                                    else ErrorRed.copy(alpha = 0.85f)
                                )
                                .clickable { isCameraEnabled = !isCameraEnabled }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isCameraEnabled) "Turn off camera" else "Turn on camera"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ListGroup {
                    ListRow(
                        title = stringResource(R.string.device_setup_mic_label),
                        supporting = stringResource(R.string.device_setup_mic_value),
                        icon = Icons.Default.Mic,
                        trailing = {
                            if (isMicEnabled) {
                                MicLevelMeter(micLevel)
                            } else {
                                Text("Muted", style = MaterialTheme.typography.labelLarge, color = ErrorRed)
                            }
                        }
                    )
                    ListDivider()
                    ListRow(
                        title = stringResource(R.string.device_setup_speaker_label),
                        supporting = if (isSpeakerMissing) "$selectedSpeaker · not connected" else selectedSpeaker,
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        onClick = { isSpeakerMenuExpanded = true },
                        trailing = {
                            TextButton(
                                enabled = !isTestingSpeaker,
                                onClick = {
                                    isTestingSpeaker = true
                                    coroutineScope.launch {
                                        if (!playTestTone(context, selectedSpeaker)) isSpeakerMissing = true
                                        isTestingSpeaker = false
                                    }
                                }
                            ) {
                                Text(if (isTestingSpeaker) "Playing…" else stringResource(R.string.device_setup_test_button))
                            }
                        }
                    )
                    ListDivider()
                    ListRow(
                        title = stringResource(R.string.device_setup_camera_label),
                        supporting = selectedCamera,
                        icon = Icons.Default.Videocam,
                        onClick = { isCameraMenuExpanded = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isSpeakerMenuExpanded) {
                DevicePickerSheet(
                    title = stringResource(R.string.device_setup_speaker_label),
                    options = listOf("Speaker", "Earpiece", "Bluetooth Headset"),
                    selected = selectedSpeaker,
                    onSelect = {
                        selectedSpeaker = it
                        isSpeakerMissing = false
                    },
                    onDismiss = { isSpeakerMenuExpanded = false }
                )
            }
            if (isCameraMenuExpanded) {
                DevicePickerSheet(
                    title = stringResource(R.string.device_setup_camera_label),
                    options = listOf("Front Camera", "Back Camera"),
                    selected = selectedCamera,
                    onSelect = { selectedCamera = it },
                    onDismiss = { isCameraMenuExpanded = false }
                )
            }

            BottomActionBar {
                GradientButton(
                    text = stringResource(if (isHost) R.string.device_setup_start_session else R.string.device_setup_join_session),
                    onClick = onStartSession
                )
                // Only guests wait to be let in; the host opens the room.
                if (!isHost) {
                    Text(
                        text = stringResource(R.string.device_setup_waiting_room_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Plays a short beep through the chosen output (loudspeaker, earpiece or Bluetooth headset), the way
 * call audio will be routed. Returns false when that output isn't connected.
 */
private suspend fun playTestTone(context: Context, output: String): Boolean {
    val audioManager = context.getSystemService(AudioManager::class.java)
    val wantedType = when (output) {
        "Earpiece" -> AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        "Bluetooth Headset" -> AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        else -> AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
    }
    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    val isRouted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        audioManager.availableCommunicationDevices.firstOrNull { it.type == wantedType }?.let(audioManager::setCommunicationDevice) ?: false
    } else {
        // shortcut: before Android 12 a Bluetooth headset needs SCO set up; the tone plays on the earpiece instead.
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = output == "Speaker"
        true
    }
    if (isRouted) {
        val tone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 90)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
        delay(900)
        tone.release()
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        audioManager.clearCommunicationDevice()
    } else {
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = false
    }
    audioManager.mode = AudioManager.MODE_NORMAL
    return isRouted
}

/** Native picker for a device; picking an option closes the sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevicePickerSheet(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        options.forEach { option ->
            ListItem(
                modifier = Modifier.clickable(role = Role.RadioButton) {
                    onSelect(option)
                    onDismiss()
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(option) },
                trailingContent = if (option == selected) {
                    { Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                } else null
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Live preview from the phone camera; the camera is released when this screen stops or leaves. */
@Composable
private fun LiveCameraPreview(isFrontCamera: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    DisposableEffect(lifecycleOwner, isFrontCamera) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val selector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        var isDisposed = false
        providerFuture.addListener({
            // A phone without this camera just shows the empty preview box.
            if (!isDisposed) runCatching { providerFuture.get().bindToLifecycle(lifecycleOwner, selector, preview) }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            isDisposed = true
            // Unbind only this preview so the camera screen opened next keeps its own camera running.
            if (providerFuture.isDone) providerFuture.get().unbind(preview)
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

/** Rising bars that light up with the input level, so the user can see the mic picking up their voice. */
@Composable
private fun MicLevelMeter(level: Float) {
    // Map -60..0 dBFS onto the bars: room noise lights one or two, normal speech about half, shouting the red end.
    val decibels = 20f * log10(level.coerceAtLeast(0.001f))
    val litBars = ((decibels + 60f) / 60f * MIC_METER_BARS).roundToInt().coerceIn(0, MIC_METER_BARS)
    Row(
        modifier = Modifier.semantics { contentDescription = "Microphone level" },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(MIC_METER_BARS) { index ->
            val color = when {
                index >= litBars -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                index >= MIC_METER_BARS - 2 -> ErrorRed
                index >= MIC_METER_BARS - 4 -> WarningAmber
                else -> SuccessGreen
            }
            Box(
                Modifier
                    .size(width = 4.dp, height = 6.dp + 2.dp * index)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

private const val MIC_METER_BARS = 10

/** Shown in the preview box until camera and microphone access is allowed. */
@Composable
private fun CameraAccessPrompt(permission: CameraPermissionState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideocamOff,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Allow camera and microphone access to check your setup.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        TextButton(onClick = if (permission.wasDenied) permission.openAppSettings else permission.request) {
            Text(if (permission.wasDenied) "Open settings" else "Allow access", color = Color.White)
        }
    }
}
