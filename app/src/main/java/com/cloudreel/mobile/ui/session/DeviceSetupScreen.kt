package com.cloudreel.mobile.ui.session

import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.ui.components.GradientButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DeviceSetupScreen(
    mode: String,
    sessionCode: String,
    onNavigateBack: () -> Unit,
    onStartSession: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isMicEnabled by remember { mutableStateOf(true) }
    var isCameraEnabled by remember { mutableStateOf(true) }
    var selectedSpeaker by remember { mutableStateOf("Speaker") }
    var isSpeakerMenuExpanded by remember { mutableStateOf(false) }
    var selectedCamera by remember { mutableStateOf("Front Camera") }
    var isCameraMenuExpanded by remember { mutableStateOf(false) }
    var isTestingSpeaker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar with Back (<) and Theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onNavigateBack)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate back"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Circular Theme / Moon Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            Toast.makeText(context, "Theme toggled", Toast.LENGTH_SHORT).show()
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Toggle Theme"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title: Device Setup
                Text(
                    text = stringResource(R.string.device_setup_title),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: Check your camera and microphone
                Text(
                    text = stringResource(R.string.device_setup_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── CAMERA PREVIEW VIEWPORT ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF16161D))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                ) {
                    if (isCameraEnabled) {
                        // Simulated Front Camera Viewport matching screenshot
                        SimulatedCameraViewport(
                            isFrontCamera = selectedCamera == "Front Camera"
                        )
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
                                fontSize = 14.sp,
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
                            fontSize = 12.sp,
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
                                .clickable {
                                    isMicEnabled = !isMicEnabled
                                    val msg = if (isMicEnabled) "Microphone enabled" else "Microphone muted"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
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
                                .clickable {
                                    isCameraEnabled = !isCameraEnabled
                                    val msg = if (isCameraEnabled) "Camera enabled" else "Camera turned off"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
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

                // ── DEVICE CONFIGURATION CARD ────────────────────────────────
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // 1. Microphone Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.device_setup_mic_label),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.device_setup_mic_value),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )

                        // 2. Speaker Row (with Dropdown and Test button)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier
                                        .clickable { isSpeakerMenuExpanded = true }
                                ) {
                                    Text(
                                        text = stringResource(R.string.device_setup_speaker_label),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = selectedSpeaker,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CRPurple
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select Speaker",
                                            tint = CRPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isSpeakerMenuExpanded,
                                    onDismissRequest = { isSpeakerMenuExpanded = false }
                                ) {
                                    listOf("Speaker", "Earpiece", "Bluetooth Headset").forEach { speaker ->
                                        DropdownMenuItem(
                                            text = { Text(speaker) },
                                            onClick = {
                                                selectedSpeaker = speaker
                                                isSpeakerMenuExpanded = false
                                            },
                                            trailingIcon = if (selectedSpeaker == speaker) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = CRPurple) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            // Vertical Divider between speaker and Test button
                            VerticalDivider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )

                            // Test Button
                            Text(
                                text = if (isTestingSpeaker) "Playing…" else stringResource(R.string.device_setup_test_button),
                                color = CRPurple,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isTestingSpeaker = true
                                        Toast.makeText(context, "Playing audio test chime through $selectedSpeaker", Toast.LENGTH_SHORT).show()
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                                                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 350)
                                            } catch (_: Exception) {}
                                            kotlinx.coroutines.delay(1000)
                                            isTestingSpeaker = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )

                        // 3. Camera Row (with Dropdown)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier
                                        .clickable { isCameraMenuExpanded = true }
                                ) {
                                    Text(
                                        text = stringResource(R.string.device_setup_camera_label),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = selectedCamera,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CRPurple
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select Camera",
                                            tint = CRPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isCameraMenuExpanded,
                                    onDismissRequest = { isCameraMenuExpanded = false }
                                ) {
                                    listOf("Front Camera", "Back Camera").forEach { camera ->
                                        DropdownMenuItem(
                                            text = { Text(camera) },
                                            onClick = {
                                                selectedCamera = camera
                                                isCameraMenuExpanded = false
                                            },
                                            trailingIcon = if (selectedCamera == camera) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = CRPurple) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // ── PRIMARY ACTION: START SESSION ────────────────────────────
                GradientButton(
                    text = stringResource(R.string.device_setup_start_session),
                    onClick = onStartSession,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footnote: You may be placed in a waiting room
                Text(
                    text = stringResource(R.string.device_setup_waiting_room_notice),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Realistic Simulated Camera Viewport matching the host user screenshot in mask
 */
@Composable
fun SimulatedCameraViewport(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E24)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background room / wall shading
            drawRect(
                color = Color(0xFFF2EFE9),
                topLeft = Offset(width * 0.18f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.64f, height)
            )

            // Ceiling / wall line
            drawLine(
                color = Color(0xFFDED8CE),
                start = Offset(width * 0.18f, height * 0.35f),
                end = Offset(width * 0.82f, height * 0.28f),
                strokeWidth = 3f
            )

            // Shoulders & Shirt (Warm brick/maroon tee)
            drawOval(
                color = Color(0xFFB57070),
                topLeft = Offset(width * 0.22f, height * 0.72f),
                size = androidx.compose.ui.geometry.Size(width * 0.56f, height * 0.45f)
            )

            // Neck
            drawRect(
                color = Color(0xFFC9987C),
                topLeft = Offset(width * 0.42f, height * 0.62f),
                size = androidx.compose.ui.geometry.Size(width * 0.16f, height * 0.15f)
            )

            // Head / Forehead / Hair
            // Hair (Dark textured curls)
            drawOval(
                color = Color(0xFF2B221E),
                topLeft = Offset(width * 0.33f, height * 0.32f),
                size = androidx.compose.ui.geometry.Size(width * 0.34f, height * 0.22f)
            )

            // Face (Forehead / eyes area)
            drawOval(
                color = Color(0xFFC9987C),
                topLeft = Offset(width * 0.35f, height * 0.37f),
                size = androidx.compose.ui.geometry.Size(width * 0.30f, height * 0.26f)
            )

            // Hair overlay
            drawArc(
                color = Color(0xFF221A17),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(width * 0.32f, height * 0.31f),
                size = androidx.compose.ui.geometry.Size(width * 0.36f, height * 0.16f)
            )

            // Eyes / Eyebrows
            drawArc(
                color = Color(0xFF32231A),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(width * 0.40f, height * 0.42f),
                size = androidx.compose.ui.geometry.Size(width * 0.07f, height * 0.02f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
            drawArc(
                color = Color(0xFF32231A),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(width * 0.53f, height * 0.42f),
                size = androidx.compose.ui.geometry.Size(width * 0.07f, height * 0.02f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Protective Face Mask (Dark pleated mask covering lower half)
            drawOval(
                color = Color(0xFF1E2024),
                topLeft = Offset(width * 0.34f, height * 0.46f),
                size = androidx.compose.ui.geometry.Size(width * 0.32f, height * 0.22f)
            )

            // Mask contour lines
            drawLine(
                color = Color(0xFF2E323A),
                start = Offset(width * 0.36f, height * 0.52f),
                end = Offset(width * 0.64f, height * 0.52f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFF2E323A),
                start = Offset(width * 0.38f, height * 0.57f),
                end = Offset(width * 0.62f, height * 0.57f),
                strokeWidth = 2.5f
            )

            // Mask Ear straps
            drawLine(
                color = Color(0xFF4A4E58),
                start = Offset(width * 0.35f, height * 0.50f),
                end = Offset(width * 0.32f, height * 0.46f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFF4A4E58),
                start = Offset(width * 0.65f, height * 0.50f),
                end = Offset(width * 0.68f, height * 0.46f),
                strokeWidth = 3f
            )
        }
    }
}
