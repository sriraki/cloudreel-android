package com.cloudreel.mobile.ui.session

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRBrandGradient
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge
import com.cloudreel.mobile.ui.components.GradientButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SessionEntryMode {
    HOST,
    PARTICIPANT;

    companion object {
        fun fromString(value: String?): SessionEntryMode {
            return if (value.equals("host", ignoreCase = true)) HOST else PARTICIPANT
        }
    }
}

/**
 * Shared URL and Room Code Entry Screen for Host and Participant modes.
 * Dynamically adapts titles, instructions, hints, validation, and action labels.
 */
@Composable
fun SessionEntryScreen(
    mode: String,
    onNavigateBack: () -> Unit,
    onValidationSuccess: (String) -> Unit
) {
    val entryMode = SessionEntryMode.fromString(mode)
    val isHost = entryMode == SessionEntryMode.HOST

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var inputCode by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isValidating by remember { mutableStateOf(false) }

    fun validateAndProceed() {
        val trimmed = inputCode.trim()
        if (trimmed.isBlank()) {
            errorMessage = "Please enter a valid session URL or room code."
            return
        }

        // Validate URL or Room Code (4-24 chars, alphanumeric + dashes)
        val isUrl = trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
        val isRoomCode = trimmed.matches(Regex("^[a-zA-Z0-9_-]{4,24}$"))

        if (!isUrl && !isRoomCode) {
            errorMessage = "Invalid format. Enter a secure URL (https://…) or a 4–16 character room code (e.g. CR-84920)."
            return
        }

        errorMessage = null
        isValidating = true
        keyboardController?.hide()
        focusManager.clearFocus()

        coroutineScope.launch {
            // Simulated validation roundtrip to verify session existence/contract
            delay(650)
            isValidating = false
            onValidationSuccess(trimmed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Back button and CloudReel Logo
            CloudReelHeader(
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .imePadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Mode Badge
                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(20.dp))

                // Role Icon + Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(CRBrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isHost) Icons.Default.Podcasts else Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isHost) {
                                stringResource(R.string.host_mode_title)
                            } else {
                                stringResource(R.string.participant_mode_title)
                            },
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isHost) {
                                stringResource(R.string.host_mode_subtitle)
                            } else {
                                stringResource(R.string.participant_mode_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Input Card Container
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
                        Text(
                            text = stringResource(R.string.session_code_label),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (errorMessage != null) ErrorRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it
                                if (errorMessage != null) errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (isHost) "e.g. CR-90124 or https://cloudreel.com/live" else "e.g. CR-84920 or session URL",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = if (errorMessage != null) ErrorRed else CRPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (inputCode.isNotEmpty() && !isValidating) {
                                    IconButton(onClick = {
                                        inputCode = ""
                                        errorMessage = null
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear input",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            enabled = !isValidating,
                            isError = errorMessage != null,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CRViolet,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                errorBorderColor = ErrorRed,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = { validateAndProceed() }
                            )
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = ErrorRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick demo code helper chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick Fill:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val sampleCodes = if (isHost) listOf("CR-HOST-01", "CR-BROADCAST") else listOf("CR-84920", "CR-ROOM-77")
                            for (code in sampleCodes) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .clickable(enabled = !isValidating) {
                                            inputCode = code
                                            errorMessage = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CRPurple
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Button (Continue as Host / Join Session)
                GradientButton(
                    text = if (isHost) {
                        stringResource(R.string.host_mode_button)
                    } else {
                        stringResource(R.string.participant_mode_button)
                    },
                    onClick = { validateAndProceed() },
                    isLoading = isValidating,
                    enabled = !isValidating,
                    showTrailingArrow = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Security & Protocol Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isHost) {
                            "Encrypted peer-to-peer session signaling via CloudReel Mesh"
                        } else {
                            "End-to-end encrypted session stream reception with low latency"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
