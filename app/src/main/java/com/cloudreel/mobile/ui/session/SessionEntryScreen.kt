package com.cloudreel.mobile.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRPurple
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.forTheme
import com.cloudreel.mobile.ui.components.BottomActionBar
import com.cloudreel.mobile.ui.components.CloudReelTopBar
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
            CloudReelTopBar(
                title = if (isHost) {
                    stringResource(R.string.host_mode_title)
                } else {
                    stringResource(R.string.participant_mode_title)
                },
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.session_code_label),
                        style = MaterialTheme.typography.bodyMedium,
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
                                text = if (isHost) "e.g. CR-90124 or session URL" else "e.g. CR-84920 or session URL",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = if (errorMessage != null) ErrorRed else CRPurple.forTheme(),
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
                            focusedBorderColor = CRViolet.forTheme(),
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
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            BottomActionBar {
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
            }
        }
    }
}
