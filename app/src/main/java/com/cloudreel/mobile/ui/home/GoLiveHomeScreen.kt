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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Podcasts
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
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge

@Composable
fun GoLiveHomeScreen(
    viewModel: HomeViewModel,
    onNavigateToHost: () -> Unit,
    onNavigateToParticipant: () -> Unit,
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
                showSignOut = uiState.user != null,
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
                Spacer(modifier = Modifier.height(14.dp))

                // Enterprise Platform Badge matching brand identity
                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(18.dp))

                // Heading matching specification: "Welcome to CloudReel"
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Supporting text matching specification: "Choose how you want to connect."
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── OPTION 1: START AS HOST ───────────────────────────────────────
                HomeOptionCard(
                    title = stringResource(R.string.start_as_host_title),
                    description = stringResource(R.string.start_as_host_subtitle),
                    badgeLabel = "Host Mode",
                    icon = Icons.Default.Podcasts,
                    accentColor = CRPurple,
                    onClick = onNavigateToHost
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── OPTION 2: JOIN AS PARTICIPANT ─────────────────────────────────
                HomeOptionCard(
                    title = stringResource(R.string.join_as_participant_title),
                    description = stringResource(R.string.join_as_participant_subtitle),
                    badgeLabel = "Receiver Mode",
                    icon = Icons.Default.Group,
                    accentColor = CRViolet,
                    onClick = onNavigateToParticipant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── OPTION 3: STREAMER ───────────────────────────────────────────
                HomeOptionCard(
                    title = stringResource(R.string.streamer_title),
                    description = stringResource(R.string.streamer_description),
                    badgeLabel = "Camera + Bonding",
                    icon = Icons.Default.Videocam,
                    accentColor = CRMagenta,
                    onClick = onNavigateToStreamer
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── OPTION 4: RELAY ──────────────────────────────────────────────
                HomeOptionCard(
                    title = stringResource(R.string.relay_title),
                    description = stringResource(R.string.relay_description),
                    badgeLabel = "Donate This Cellular",
                    icon = Icons.Default.CellTower,
                    accentColor = CRPurple,
                    onClick = onNavigateToRelay
                )

                Spacer(modifier = Modifier.height(26.dp))

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
 * Enterprise Home Option Card component with full accessibility, tactile feedback,
 * brand color tokens, and comfortable touch targets.
 */
@Composable
private fun HomeOptionCard(
    title: String,
    description: String,
    badgeLabel: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        label = "homeCardScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
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
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Bonded Network preview card matching the brand telemetry card in Screenshot 2.
 */
@Composable
private fun BondedNetworkPreviewCard(
    bondingState: NetworkBondingState
) {
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = CRPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CELLULAR BONDING MESH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(8.dp)
                    )
                    Text(
                        text = "READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    icon = Icons.Default.Wifi,
                    label = "WiFi Uplink",
                    value = if (bondingState.wifiConnected) "18.4 Mbps" else "Standby"
                )
                MetricColumn(
                    icon = Icons.Default.CellTower,
                    label = "Cellular 5G",
                    value = if (bondingState.cellular5GConnected) "24.1 Mbps" else "Ready"
                )
                MetricColumn(
                    icon = Icons.Default.NetworkCheck,
                    label = "Total Bonded",
                    value = "${bondingState.bondedSpeedMbps} Mbps"
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CRPurple,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
