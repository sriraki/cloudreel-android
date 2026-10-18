package com.cloudreel.mobile.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRBrandGradient
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge

@Composable
fun GoLiveHomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStreamer: () -> Unit,
    onNavigateToRelay: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with CloudReel logo top-left, theme toggle and sign out top-right
            CloudReelHeader(
                showSignOut = true,
                onSignOutClick = {
                    viewModel.logout()
                    onLogout()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Enterprise Badge matching brand reference
                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(20.dp))

                // Headline Title matching specification
                Text(
                    text = stringResource(R.string.go_live_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.go_live_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── OPTION A: STREAMER ─────────────────────────────────────────
                RoleOptionCard(
                    title = stringResource(R.string.streamer_title),
                    description = stringResource(R.string.streamer_description),
                    badgeLabel = "Camera + Bonding",
                    icon = Icons.Default.Videocam,
                    isPrimaryGradient = true,
                    onClick = onNavigateToStreamer
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ── OPTION B: RELAY ───────────────────────────────────────────
                RoleOptionCard(
                    title = stringResource(R.string.relay_title),
                    description = stringResource(R.string.relay_description),
                    badgeLabel = "Donate This Cellular",
                    icon = Icons.Default.CellTower,
                    isPrimaryGradient = false,
                    onClick = onNavigateToRelay
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── BONDED TELEMETRY CARD (from brand screenshot 2 card aesthetic) ──
                BondedNetworkPreviewCard(
                    bondingState = uiState.bondingState
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

/**
 * Enterprise Role Option Card component with full accessibility and interactive tactile feedback.
 */
@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    badgeLabel: String,
    icon: ImageVector,
    isPrimaryGradient: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        label = "roleCardScale"
    )

    val surfaceColor = if (isPrimaryGradient) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface
    }

    val outlineColor = if (isPrimaryGradient) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline
    }

    val titleColor = if (isPrimaryGradient) Color.White else MaterialTheme.colorScheme.onBackground
    val descColor = if (isPrimaryGradient) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isPrimaryGradient) {
                    Modifier.background(CRBrandGradient)
                } else {
                    Modifier
                        .background(surfaceColor)
                        .border(1.dp, outlineColor, RoundedCornerShape(22.dp))
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = SemanticsRole.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = "$title, $badgeLabel. $description"
                role = SemanticsRole.Button
            }
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPrimaryGradient) Color.White.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPrimaryGradient) Color.White else CRPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Role badge pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPrimaryGradient) Color.White.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPrimaryGradient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = descColor
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isPrimaryGradient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 4.dp)
            )
        }
    }
}

/**
 * Live bonded cellular status preview matching the video preview card in Screenshot 2
 */
@Composable
private fun BondedNetworkPreviewCard(
    bondingState: com.cloudreel.mobile.ui.home.NetworkBondingState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "CELLULAR BONDING READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Latency pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${bondingState.latencyMs}ms Latency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Network telemetry stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetric(
                    label = "5G Cellular",
                    value = if (bondingState.cellular5GConnected) "Active" else "Offline",
                    icon = Icons.Default.CellTower
                )
                TelemetryMetric(
                    label = "Wi-Fi 6",
                    value = if (bondingState.wifiConnected) "Bonded" else "Offline",
                    icon = Icons.Default.Wifi
                )
                TelemetryMetric(
                    label = "Total Throughput",
                    value = "${bondingState.bondedSpeedMbps} Mbps",
                    icon = Icons.Default.NetworkCheck
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetric(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
