package com.cloudreel.mobile.ui.category

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRBrandGradient
import com.cloudreel.mobile.theme.CRIndigo
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.ListDivider
import com.cloudreel.mobile.ui.components.ListGroup
import com.cloudreel.mobile.ui.components.ListRow
import com.cloudreel.mobile.ui.components.ListSectionTitle
import com.cloudreel.mobile.ui.home.HomeViewModel
import com.cloudreel.mobile.ui.home.NetworkBondingState
import kotlin.math.roundToInt

/**
 * Home: one front door for each kind of user (viewers join a channel's debate, reporters go live),
 * then one-tap rows for hosting, relaying and network contribution.
 */
@Composable
fun CategorySelectionScreen(
    viewModel: HomeViewModel,
    onJoinWithCode: () -> Unit,
    onGoLive: () -> Unit,
    onHostDebate: () -> Unit,
    onRelay: () -> Unit,
    onNetworkContribution: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            JoinDebateCard(onClick = onJoinWithCode)
            Spacer(modifier = Modifier.height(16.dp))
            ReportLiveCard(bondingState = uiState.bondingState, onGoLive = onGoLive)

            ListSectionTitle("More ways to take part")
            ListGroup {
                ListRow(
                    title = stringResource(R.string.start_as_host_title),
                    supporting = stringResource(R.string.start_as_host_subtitle),
                    icon = Icons.Default.Podcasts,
                    accentColor = CRPurple,
                    onClick = onHostDebate
                )
                ListDivider()
                ListRow(
                    title = stringResource(R.string.relay_title),
                    supporting = stringResource(R.string.relay_description),
                    icon = Icons.Default.CellTower,
                    accentColor = CRIndigo,
                    onClick = onRelay
                )
                ListDivider()
                ListRow(
                    title = stringResource(R.string.mesh_title),
                    supporting = stringResource(R.string.mesh_description),
                    icon = Icons.Default.NetworkCheck,
                    accentColor = CRMagenta,
                    onClick = onNetworkContribution
                )
            }
        }
    }
}

/** For viewers: the brand card with the faces of a live debate and one obvious button. */
@Composable
private fun JoinDebateCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CRBrandGradient)
            .clickable(interactionSource = null, indication = ripple(color = Color.White), onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarStack(listOf(R.drawable.category_host, R.drawable.category_guest_1, R.drawable.category_guest_2))
            Spacer(modifier = Modifier.weight(1f))
            LivePill(background = Color.Black.copy(alpha = 0.25f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Join a debate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Enter the code from your channel and take part live.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CRPurple)
        ) {
            Text("Enter code", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

/** For reporters: a field shot, whether the bonded network is ready, and a red Go live button like the camera's. */
@Composable
private fun ReportLiveCard(bondingState: NetworkBondingState, onGoLive: () -> Unit) {
    val isNetworkReady = bondingState.wifiConnected || bondingState.cellular5GConnected
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = null, indication = ripple(color = Color.White), onClick = onGoLive)
    ) {
        Image(
            painter = painterResource(R.drawable.category_broadcast),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        // Dark fade from the left so the text reads over the photo.
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.85f), 0.75f to Color.Transparent))
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (isNetworkReady) SuccessGreen else Color.White.copy(alpha = 0.5f)))
                Text(
                    text = if (isNetworkReady) "Network ready · ${bondingState.bondedSpeedMbps.roundToInt()} Mbps" else "Checking network…",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }
            // Text stays on the left so the reporter's face on the right stays visible; the network label above may be wider.
            Column(modifier = Modifier.fillMaxWidth(0.66f)) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Report live", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Stream from any location for your channel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onGoLive,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go live", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Overlapping round photos, like the participants already in a session. */
@Composable
private fun AvatarStack(photos: List<Int>) {
    Row {
        photos.forEachIndexed { index, photo ->
            Image(
                painter = painterResource(photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(x = (-12).dp * index)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

/** LIVE label with a slowly pulsing red dot. */
@Composable
private fun LivePill(background: Color) {
    val dotAlpha by rememberInfiniteTransition(label = "liveDot").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveDotAlpha"
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(7.dp).alpha(dotAlpha).clip(CircleShape).background(ErrorRed))
        Text("LIVE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
