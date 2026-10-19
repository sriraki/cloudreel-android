package com.cloudreel.mobile.ui.session

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun ActiveSessionScreen(
    mode: String,
    sessionCode: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Session controls & states
    var isMuted by remember { mutableStateOf(false) }
    var isVideoStopped by remember { mutableStateOf(false) }
    var isSharingScreen by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNetworkBanner by remember { mutableStateOf(true) }

    // Live session timer in seconds
    var elapsedSeconds by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val formattedTime = String.format(
        "%02d:%02d",
        elapsedSeconds / 60,
        elapsedSeconds % 60
    )

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
            // ── 1. TOP HEADER STATUS BAR ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Timer and Signal Strength Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer pill: ⏱ 00:05
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Call timer",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = formattedTime,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Green network strength pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Signal Strength",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Right: Settings, Theme Toggle, Participants Count (👥 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable { showSettingsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Session Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Dark/Light Theme button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable {
                                Toast.makeText(context, "Theme toggled", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Participants button: 👥 1
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable { showParticipantsDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Participants",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "1",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // ── 2. ACTIVE VIDEO STAGE (WITH BRIGHT GREEN ACTIVE BORDER) ───────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(if (isFullscreen) 16f / 11f else 16f / 9.5f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF14141A))
                    .border(
                        width = 3.dp,
                        color = Color(0xFF10B981), // Vibrant active speaker border from screenshot 2
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                if (!isVideoStopped) {
                    // Realistic Hallway / Video Viewport matching screenshot 2
                    ActiveLiveVideoViewport(isFrontCamera = isFrontCamera)
                } else {
                    // Video Stopped placeholder
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF282834)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Camera is turned off",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }

                // Video Overlay - Top Left: Active Green Microphone
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = if (!isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (!isMuted) "Active Audio" else "Muted Audio",
                        tint = if (!isMuted) Color(0xFF10B981) else ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Video Overlay - Top Right: Signal strength & Fullscreen
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal Strength",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )

                    // Fullscreen Toggle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable {
                                isFullscreen = !isFullscreen
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Video Overlay - Bottom Left: "HOST" and "You" Pill Badges
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HOST Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4338CA))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.active_session_badge_host),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // You Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.active_session_badge_you),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Video Overlay - Bottom Right: Camera Flip Button (🔄)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable {
                            isFrontCamera = !isFrontCamera
                            val camName = if (isFrontCamera) "Front Camera" else "Rear Camera"
                            Toast.makeText(context, "Switched to $camName", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Optional Recording Indicator
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ErrorRed.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = "REC",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.35f))

            // ── 3. FLOATING NETWORK BANNER: "Switched to mobile network" ──────
            AnimatedVisibility(
                visible = showNetworkBanner,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .clickable { showNetworkBanner = false }
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_session_switched_network),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. BOTTOM ACTION CONTROL BAR (5 CONTROLS) ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Mute / Unmute
                SessionControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) stringResource(R.string.active_session_unmute) else stringResource(R.string.active_session_mute),
                    iconTint = if (isMuted) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    textColor = if (isMuted) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        isMuted = !isMuted
                        Toast.makeText(context, if (isMuted) "Audio Muted" else "Audio Unmuted", Toast.LENGTH_SHORT).show()
                    }
                )

                // 2. Stop / Start Video
                SessionControlButton(
                    icon = if (isVideoStopped) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    label = if (isVideoStopped) stringResource(R.string.active_session_start_video) else stringResource(R.string.active_session_stop),
                    iconTint = if (isVideoStopped) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    textColor = if (isVideoStopped) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        isVideoStopped = !isVideoStopped
                        Toast.makeText(context, if (isVideoStopped) "Camera Stopped" else "Camera Resumed", Toast.LENGTH_SHORT).show()
                    }
                )

                // 3. Share Screen
                SessionControlButton(
                    icon = if (isSharingScreen) Icons.AutoMirrored.Filled.StopScreenShare else Icons.AutoMirrored.Filled.ScreenShare,
                    label = stringResource(R.string.active_session_share),
                    iconTint = if (isSharingScreen) CRPurple else MaterialTheme.colorScheme.onSurface,
                    textColor = if (isSharingScreen) CRPurple else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        isSharingScreen = !isSharingScreen
                        val msg = if (isSharingScreen) "Screen sharing started" else "Screen sharing stopped"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                // 4. Record Session
                SessionControlButton(
                    icon = Icons.Default.FiberManualRecord,
                    label = stringResource(R.string.active_session_record),
                    iconTint = if (isRecording) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    textColor = if (isRecording) ErrorRed else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        isRecording = !isRecording
                        val msg = if (isRecording) "Recording started" else "Recording saved"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                // 5. Leave Call (Red)
                SessionControlButton(
                    icon = Icons.Default.CallEnd,
                    label = stringResource(R.string.active_session_leave),
                    iconTint = ErrorRed,
                    textColor = ErrorRed,
                    onClick = { showLeaveDialog = true }
                )
            }
        }
    }

    // ── LEAVE SESSION DIALOG ───────────────────────────────────────────────
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Session?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to leave this session? As the host, other participants may remain in the room.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Leave Room", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── PARTICIPANTS DIALOG ───────────────────────────────────────────────
    if (showParticipantsDialog) {
        AlertDialog(
            onDismissRequest = { showParticipantsDialog = false },
            title = { Text("Active Participants (1)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("You (Host)", fontWeight = FontWeight.SemiBold)
                            Text("Cellular Bonding Mesh · 42 Mbps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Host", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParticipantsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ── SETTINGS DIALOG ───────────────────────────────────────────────────
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Live Session Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Session Code: ${sessionCode.ifBlank { "CR-84920" }}", fontSize = 13.sp)
                    Text("• Video Profile: 1080p60 FHD (SRT Protocol)", fontSize = 13.sp)
                    Text("• Audio Codec: Opus 48kHz Stereo", fontSize = 13.sp)
                    Text("• Network Bonding: Multi-SIM Cellular Active", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SessionControlButton(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Realistic Live Video Viewport matching screenshot 2 (office corridor / hallway background)
 */
@Composable
private fun ActiveLiveVideoViewport(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14141A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Center corridor floor & walls
            drawRect(
                color = Color(0xFFC7BBAA),
                topLeft = Offset(width * 0.35f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.30f, height)
            )

            // Left dark door frame
            drawRect(
                color = Color(0xFF5C3C24),
                topLeft = Offset(width * 0.35f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.05f, height * 0.52f)
            )

            // Right pale yellow doorway
            drawRect(
                color = Color(0xFFE8E0B0),
                topLeft = Offset(width * 0.58f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.07f, height * 0.45f)
            )

            // Baseboard trim
            drawRect(
                color = Color(0xFF4A4E58),
                topLeft = Offset(width * 0.58f, height * 0.42f),
                size = androidx.compose.ui.geometry.Size(width * 0.07f, height * 0.04f)
            )

            // Flooring flecked pattern
            drawLine(
                color = Color(0xFFA89C8B),
                start = Offset(width * 0.35f, height * 0.50f),
                end = Offset(width * 0.65f, height * 0.50f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFFA89C8B),
                start = Offset(width * 0.35f, height * 0.70f),
                end = Offset(width * 0.65f, height * 0.70f),
                strokeWidth = 2f
            )

            // Left & Right Dark Letterboxing
            drawRect(
                color = Color(0xFF0D0D12),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.35f, height)
            )
            drawRect(
                color = Color(0xFF0D0D12),
                topLeft = Offset(width * 0.65f, 0f),
                size = androidx.compose.ui.geometry.Size(width * 0.35f, height)
            )
        }
    }
}
