package com.cloudreel.mobile.ui.streamer

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.theme.WarningAmber
import com.cloudreel.mobile.ui.components.CloudReelTopBar

private val AvatarColors = listOf(Color(0xFF7928CA), Color(0xFF3031CB), Color(0xFF0E7490), Color(0xFFB45309), Color(0xFF15803D), Color(0xFFBE185D))

/** Portrait: the gallery fills the screen. Landscape: a 16:9 frame. */
fun programFrameSize(width: Dp, height: Dp, isLandscape: Boolean): DpSize {
    if (!isLandscape) return DpSize(width, height)
    val frameWidth = minOf(width, height * 16f / 9f)
    return DpSize(frameWidth, frameWidth * 9f / 16f)
}

// clipToBounds: the camera preview's TextureView is larger than its tile and would otherwise draw past it.
private fun Modifier.inCell(cell: FrameCell, frame: DpSize) = this
    .offset(x = frame.width * cell.x, y = frame.height * cell.y)
    .size(frame.width * cell.width, frame.height * cell.height)
    .padding(1.dp)
    .clipToBounds()

/**
 * The meeting gallery: "You" first, then everyone else, in tiles that rearrange as people join or leave.
 * [selfPreview] draws the camera; it is always called from this one place so the preview view is never re-parented.
 * [onGuestTap] is null when tiles do nothing (guests can't manage others).
 */
@Composable
fun MeetingGallery(
    state: StreamerUiState,
    isLandscape: Boolean,
    isSelfSpeaking: Boolean,
    isScreenSharing: Boolean,
    onGuestTap: ((Guest) -> Unit)?,
    onBackgroundTap: () -> Unit,
    selfPreview: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        val frame = programFrameSize(maxWidth, maxHeight, isLandscape)
        Box(modifier = Modifier.align(Alignment.Center).size(frame).background(Color(0xFF0E0E12))) {
            val cells = galleryCells(state.guests.size + 1, isLandscape)
            val selfCell = cells.first()
            val isSelfCompact = selfCell.width < 0.34f || selfCell.height < 0.34f
            selfPreview(Modifier.inCell(selfCell, frame))
            if (state.isCameraOff || isScreenSharing) {
                Box(
                    modifier = Modifier
                        .inCell(selfCell, frame)
                        .background(Color(0xFF16161D))
                        .pointerInput(Unit) { detectTapGestures(onTap = { onBackgroundTap() }) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isScreenSharing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ScreenShare, contentDescription = null, tint = Color.White, modifier = Modifier.size(if (isSelfCompact) 24.dp else 40.dp))
                            if (!isSelfCompact) Text("You're sharing your screen", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    } else {
                        InitialsAvatar("You", CRMagenta, if (isSelfCompact) 36.dp else 56.dp)
                    }
                }
            }
            // Speaking ring, as in a meeting app: your tile glows green while your mic picks up voice.
            // Nothing here handles touches, so taps still reach the camera below.
            Box(Modifier.inCell(selfCell, frame).then(if (isSelfSpeaking) Modifier.border(3.dp, SuccessGreen) else Modifier)) {
                NameTag(name = "You", isMuted = state.isMicMuted, modifier = Modifier.align(Alignment.BottomStart))
            }
            state.guests.forEachIndexed { index, guest ->
                val cell = cells[index + 1]
                GuestTile(
                    guest = guest,
                    isCompact = cell.width < 0.34f || cell.height < 0.34f,
                    modifier = Modifier
                        .inCell(cell, frame)
                        .then(if (onGuestTap != null) Modifier.clickable { onGuestTap(guest) } else Modifier)
                )
            }
        }
    }
}

@Composable
private fun NameTag(name: String, isMuted: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(OverlayColor)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (isMuted) Icon(Icons.Filled.MicOff, contentDescription = "Muted", tint = ErrorRed, modifier = Modifier.size(10.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GuestTile(guest: Guest, isCompact: Boolean, modifier: Modifier) {
    Box(modifier = modifier.background(Color(0xFF16161D)), contentAlignment = Alignment.Center) {
        if (guest.photo != null) {
            Image(painterResource(guest.photo), contentDescription = guest.name, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        } else {
            InitialsAvatar(guest.initials, AvatarColors[guest.id % AvatarColors.size], if (isCompact) 28.dp else 48.dp)
        }
        NameTag(
            name = if (isCompact) guest.name.substringBefore(' ') else guest.name,
            isMuted = guest.isMuted,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun InitialsAvatar(text: String, color: Color, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(text, style = if (size > 36.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun GuestAvatar(guest: Guest, size: Dp = 40.dp) {
    if (guest.photo != null) {
        Image(painterResource(guest.photo), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(CircleShape))
    } else {
        InitialsAvatar(guest.initials, AvatarColors[guest.id % AvatarColors.size], size)
    }
}

/**
 * Meeting controls over the gallery, as in Teams or Zoom. Top: call time, switch camera, people.
 * Bottom: video, mic, then (host only) share and record, and the red end button.
 * The screen-share and waiting-room banners stay visible when the bars hide.
 */
@Composable
fun MeetingBars(
    state: StreamerUiState,
    isVisible: Boolean,
    callSeconds: Int,
    isScreenSharing: Boolean,
    onFlip: () -> Unit,
    onOpenPeople: (ParticipantsTab) -> Unit,
    onToggleVideo: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleScreenShare: () -> Unit,
    onRecord: () -> Unit,
    onLeave: () -> Unit,
    onAdmit: (Guest) -> Unit,
    onDeny: (Guest) -> Unit
) {
    val topBarAlpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "topBarAlpha")
    Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            // The top bar fades but keeps its space, so the banners below never jump under a finger aimed at People.
            // While hidden its buttons are off, so taps fall through to the gallery and bring the bars back.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = topBarAlpha }
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopPill {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(formatCallTime(callSeconds), style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onFlip, enabled = isVisible) { Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch camera", tint = Color.White) }
                Box {
                    TopPill(onClick = if (isVisible) { { onOpenPeople(ParticipantsTab.IN_CALL) } } else null) {
                        Icon(Icons.Filled.Group, contentDescription = "People", tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("${state.guests.size + 1}", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    if (state.waitingGuests.isNotEmpty()) {
                        Box(Modifier.align(Alignment.TopEnd).size(10.dp).clip(CircleShape).background(WarningAmber))
                    }
                }
            }
            val bannerModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
            if (isScreenSharing) {
                Box(bannerModifier, contentAlignment = Alignment.Center) { ScreenShareBanner(onStop = onToggleScreenShare) }
            }
            state.waitingGuests.firstOrNull()?.let { guest ->
                Box(bannerModifier, contentAlignment = Alignment.Center) {
                    WaitingBanner(
                        guest = guest,
                        othersWaiting = state.waitingGuests.size - 1,
                        onAdmit = { onAdmit(guest) },
                        onDeny = { onDeny(guest) },
                        onOpenWaiting = { onOpenPeople(ParticipantsTab.WAITING) }
                    )
                }
            }
        }
        AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomCenter)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarButton(
                    icon = if (state.isCameraOff) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                    label = "Video",
                    isRed = state.isCameraOff,
                    onClick = onToggleVideo
                )
                BarButton(
                    icon = if (state.isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    label = "Mic",
                    isRed = state.isMicMuted,
                    onClick = onToggleMic
                )
                if (state.isHostSession) {
                    BarButton(icon = Icons.AutoMirrored.Filled.ScreenShare, label = if (isScreenSharing) "Stop share" else "Share", isRed = isScreenSharing, onClick = onToggleScreenShare)
                    BarButton(icon = Icons.Filled.FiberManualRecord, label = "Record", onClick = onRecord)
                }
                BarButton(icon = Icons.Filled.CallEnd, label = "Leave", isRed = true, onClick = onLeave)
            }
        }
    }
}

@Composable
private fun TopPill(onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun RowScope.BarButton(icon: ImageVector, label: String, onClick: () -> Unit, isRed: Boolean = false) {
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isRed) ErrorRed.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
    }
}

@Composable
private fun ScreenShareBanner(onStop: () -> Unit) {
    Row(
        modifier = Modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ErrorRed.copy(alpha = 0.92f))
            .padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.ScreenShare, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Text("You're sharing your screen", style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.weight(1f))
        TextButton(onClick = onStop) { Text("Stop", color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

/** Tap on a guest's tile: what the host can do with them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestActionsSheet(guest: Guest, viewModel: StreamerViewModel, onDismiss: () -> Unit) {
    fun act(action: () -> Unit) {
        action()
        onDismiss()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GuestAvatar(guest)
            Text(guest.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        SheetAction(if (guest.isMuted) Icons.Filled.Mic else Icons.Filled.MicOff, if (guest.isMuted) "Unmute" else "Mute") { act { viewModel.toggleGuestMute(guest) } }
        SheetAction(Icons.Filled.PersonRemove, "Remove from session", color = ErrorRed) { act { viewModel.removeGuest(guest) } }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, color: Color = Color.Unspecified, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(icon, contentDescription = null, tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else color) },
        headlineContent = { Text(label, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color) }
    )
}

/** Someone is in the waiting room: let the host decide without opening the people page. */
@Composable
fun WaitingBanner(guest: Guest, othersWaiting: Int, onAdmit: () -> Unit, onDeny: () -> Unit, onOpenWaiting: () -> Unit) {
    Row(
        // Landscape: a compact strip, not one spanning the whole frame.
        modifier = Modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onOpenWaiting)
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GuestAvatar(guest, 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(guest.name, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (othersWaiting > 0) "wants to join · +$othersWaiting more" else "wants to join",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        TextButton(onClick = onDeny) { Text("Deny", color = Color.White.copy(alpha = 0.8f)) }
        Button(onClick = onAdmit, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("Admit") }
    }
}

enum class ParticipantsTab { IN_CALL, WAITING, ADD }

/**
 * Participants as a full page over the call (the camera keeps running underneath).
 * The host gets In call / Waiting / Add; a guest sees who is in the call.
 */
@Composable
fun ParticipantsPage(state: StreamerUiState, initialTab: ParticipantsTab, viewModel: StreamerViewModel, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    var tab by remember { mutableIntStateOf(initialTab.ordinal) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            CloudReelTopBar(title = "Participants", onBackClick = onClose)
            if (state.isHostSession) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("In call (${state.guests.size + 1})") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Waiting (${state.waitingGuests.size})") })
                    Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Add") })
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                when (if (state.isHostSession) ParticipantsTab.entries[tab] else ParticipantsTab.IN_CALL) {
                    ParticipantsTab.IN_CALL -> InCallList(state, viewModel)
                    ParticipantsTab.WAITING -> WaitingList(state, viewModel)
                    ParticipantsTab.ADD -> AddParticipant(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun InCallList(state: StreamerUiState, viewModel: StreamerViewModel) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { InitialsAvatar("You", CRMagenta, 40.dp) },
        headlineContent = { Text("You") },
        supportingContent = { Text(listOfNotNull(if (state.isHostSession) "Host" else "Guest", if (state.isMicMuted) "Muted" else null).joinToString(" · ")) }
    )
    state.guests.forEach { guest ->
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = { GuestAvatar(guest) },
            headlineContent = { Text(guest.name) },
            supportingContent = if (guest.isMuted) {
                { Text("Muted") }
            } else null,
            trailingContent = if (state.isHostSession) {
                { GuestMenu(guest, viewModel) }
            } else null
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun GuestMenu(guest: Guest, viewModel: StreamerViewModel) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { isOpen = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More for ${guest.name}") }
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }) {
            DropdownMenuItem(text = { Text(if (guest.isMuted) "Unmute" else "Mute") }, onClick = {
                isOpen = false
                viewModel.toggleGuestMute(guest)
            })
            DropdownMenuItem(text = { Text("Remove from session", color = ErrorRed) }, onClick = {
                isOpen = false
                viewModel.removeGuest(guest)
            })
        }
    }
}

/** The host's side of the waiting lobby: everyone who opened their link and is waiting to be let in. */
@Composable
private fun WaitingList(state: StreamerUiState, viewModel: StreamerViewModel) {
    if (state.waitingGuests.isEmpty()) {
        Text(
            "No one is waiting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp)
        )
        return
    }
    if (state.waitingGuests.size > 1) {
        TextButton(onClick = viewModel::admitAllGuests, modifier = Modifier.padding(horizontal = 8.dp)) { Text("Admit all (${state.waitingGuests.size})") }
    }
    state.waitingGuests.forEach { guest ->
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = { GuestAvatar(guest) },
            headlineContent = { Text(guest.name) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { viewModel.denyGuest(guest) }) { Text("Deny") }
                    FilledTonalButton(onClick = { viewModel.admitGuest(guest) }) { Text("Admit") }
                }
            }
        )
    }
}

private fun shareInvite(context: Context, invite: InviteLink) {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, "Join my live debate on CloudReel: ${invite.url}")
    context.startActivity(Intent.createChooser(send, "Share invite"))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddParticipant(state: StreamerUiState, viewModel: StreamerViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var guestName by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf("720p") }
    var isJoiningDirectly by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = guestName,
            onValueChange = { guestName = it },
            label = { Text("Guest name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Video quality", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            INVITE_QUALITIES.forEach { option ->
                FilterChip(selected = quality == option, onClick = { quality = option }, label = { Text(option) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Join directly", style = MaterialTheme.typography.bodyLarge)
                Text("Skips the waiting room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = isJoiningDirectly, onCheckedChange = { isJoiningDirectly = it })
        }
        Button(
            onClick = {
                viewModel.createInvite(guestName.trim(), quality, isJoiningDirectly)
                guestName = ""
            },
            enabled = guestName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Generate link") }

        // The link just made, ready to send.
        state.invites.firstOrNull()?.let { latest ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Invite link for ${latest.guestName}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(latest.url, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(latest.url)) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy")
                    }
                    Button(onClick = { shareInvite(context, latest) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    }

    if (state.invites.isNotEmpty()) SectionTitle("All links (${state.invites.size})")
    state.invites.forEach { invite ->
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text("${invite.guestName} · ${invite.quality}${if (invite.isJoiningDirectly) " · joins directly" else ""}") },
            supportingContent = { Text(invite.url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingContent = {
                Row {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(invite.url)) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy link for ${invite.guestName}")
                    }
                    IconButton(onClick = { shareInvite(context, invite) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share link for ${invite.guestName}")
                    }
                }
            }
        )
    }
}

/** The host leaves on their own or ends the session for everyone; a guest just leaves. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveSessionSheet(isHost: Boolean, onLeave: () -> Unit, onEndForEveryone: () -> Unit, onStay: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onStay) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Leave the session?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                if (isHost) {
                    "Leave on your own and the guests stay connected, or end it for everyone and close the session. Ending can't be undone."
                } else {
                    "You can rejoin with the same link while the session is active."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            if (isHost) {
                Button(
                    onClick = onEndForEveryone,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("End for everyone") }
                OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) { Text("Leave session") }
            } else {
                Button(
                    onClick = onLeave,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Leave session") }
            }
            TextButton(onClick = onStay, modifier = Modifier.fillMaxWidth()) { Text("Stay") }
        }
    }
}
