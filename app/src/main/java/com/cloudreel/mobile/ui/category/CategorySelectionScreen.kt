package com.cloudreel.mobile.ui.category

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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Podcasts
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
import androidx.compose.ui.graphics.Brush
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
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge
import com.cloudreel.mobile.ui.home.HomeViewModel

@Composable
fun CategorySelectionScreen(
    viewModel: HomeViewModel,
    onNavigateToLiveSessions: () -> Unit,
    onNavigateToBroadcast: () -> Unit,
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
            // Header with CloudReel logo top-left, theme toggle and optional sign out top-right
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

                // Enterprise Platform Badge
                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(18.dp))

                // Screen heading matching specification: "How would you like to use CloudReel?"
                Text(
                    text = stringResource(R.string.category_selection_heading),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.category_selection_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── CATEGORY CARD 1: LIVE SESSIONS (HERO FILLED GRADIENT) ─────────
                CategoryCard(
                    title = stringResource(R.string.category_live_sessions_title),
                    description = stringResource(R.string.category_live_sessions_desc),
                    badgeLabel = "Interactive Meetings",
                    icon = Icons.Default.Group,
                    accentColor = CRPurple,
                    isFilled = true,
                    featureTags = listOf("Start as Host", "Join as Participant"),
                    onClick = onNavigateToLiveSessions
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── CATEGORY CARD 2: BROADCAST (OUTLINED STUDIO) ───────────────────
                CategoryCard(
                    title = stringResource(R.string.category_broadcast_title),
                    description = stringResource(R.string.category_broadcast_desc),
                    badgeLabel = "Live Stream & Mesh",
                    icon = Icons.Default.Podcasts,
                    accentColor = CRMagenta,
                    isFilled = false,
                    featureTags = listOf("Streamer", "Relay", "Mesh Network"),
                    onClick = onNavigateToBroadcast
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    description: String,
    badgeLabel: String,
    icon: ImageVector,
    accentColor: Color,
    isFilled: Boolean = false,
    featureTags: List<String> = emptyList(),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "cardScale"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFilled) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isFilled) {
                    Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(CRPurple, CRViolet)
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(22.dp)
                        )
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.5.dp,
                            color = accentColor.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(22.dp)
                        )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = SemanticsRole.Button
                contentDescription = "$title, $description"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon container
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) Color.White.copy(alpha = 0.22f)
                            else accentColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isFilled) Color.White else accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Subtitle Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFilled) Color.White.copy(alpha = 0.20f)
                            else accentColor.copy(alpha = 0.10f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) Color.White.copy(alpha = 0.35f)
                            else accentColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilled) Color.White else accentColor,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isFilled) Color.White else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFilled) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (featureTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (tag in featureTags) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isFilled) Color.White.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) Color.White.copy(alpha = 0.28f)
                                    else accentColor.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFilled) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Enter indicator row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Open $title",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFilled) Color.White else accentColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isFilled) Color.White else accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
