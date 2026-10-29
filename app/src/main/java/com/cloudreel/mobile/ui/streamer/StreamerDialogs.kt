package com.cloudreel.mobile.ui.streamer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.InfoBlue
import com.cloudreel.mobile.ui.components.CloudReelInputField

/** Metadata fields, shared by the tag popup and Settings > Metadata: one column, two in landscape. */
@Composable
fun MetadataForm(
    metadata: StreamMetadata,
    isEnabled: Boolean,
    onChange: (StreamMetadata) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnCount = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metadataFields(metadata).chunked(columnCount).forEach { rowFields ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowFields.forEach { field ->
                    // Disabled fields keep their text, so switching sending back on loses nothing.
                    CloudReelInputField(
                        value = field.value,
                        onValueChange = { onChange(field.update(it)) },
                        label = field.label,
                        enabled = isEnabled,
                        minLines = field.lines,
                        focusColor = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Saved metadata, read-only: what is attached to the stream right now. */
@Composable
fun MetadataSummary(metadata: StreamMetadata, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metadataFields(metadata).forEach { field ->
            Column {
                Text(field.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(field.value.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private class MetadataFieldSpec(
    val label: String,
    val value: String,
    val lines: Int = 1,
    val update: (String) -> StreamMetadata
)

private fun metadataFields(metadata: StreamMetadata) = listOf(
    MetadataFieldSpec("Slugline", metadata.slugline) { metadata.copy(slugline = it) },
    MetadataFieldSpec("Crew name", metadata.crewName) { metadata.copy(crewName = it) },
    MetadataFieldSpec("Story ID", metadata.storyId) { metadata.copy(storyId = it) },
    MetadataFieldSpec("Usage terms", metadata.usageTerms) { metadata.copy(usageTerms = it) },
    MetadataFieldSpec("Provider", metadata.provider) { metadata.copy(provider = it) },
    MetadataFieldSpec("Reporter", metadata.reporter) { metadata.copy(reporter = it) },
    MetadataFieldSpec("Keywords", metadata.keywords) { metadata.copy(keywords = it) },
    MetadataFieldSpec("Comments", metadata.comments, lines = 3) { metadata.copy(comments = it) }
)

/** Applies edited fields; the two switches are saved the moment they are flipped, so they are kept as they are now. */
fun StreamMetadata.withFieldsFrom(draft: StreamMetadata) =
    draft.copy(isEnabled = isEnabled, isPromptOnStart = isPromptOnStart)

/**
 * Opens read-only when metadata is saved (Edit to change it), straight into editing when empty.
 * Edits stay a draft until Save; Cancel or tapping outside drops them. [isGoLive] turns Save into
 * "Go live" for the ask-before-going-live flow.
 */
@Composable
fun MetadataDialog(
    metadata: StreamMetadata,
    onSave: (StreamMetadata) -> Unit,
    onDismiss: () -> Unit,
    isGoLive: Boolean = false,
    onGoLive: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(isGoLive || !metadata.hasContent()) }
    var draft by remember { mutableStateOf(metadata) }
    AlertDialog(
        onDismissRequest = onDismiss,
        // Title on the left, "send with the stream" switch on the right; the switch applies at once.
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Metadata", modifier = Modifier.weight(1f))
                Switch(
                    checked = metadata.isEnabled,
                    onCheckedChange = { onSave(metadata.copy(isEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedTrackColor = InfoBlue),
                    modifier = Modifier.semantics { contentDescription = "Send metadata with the live stream" }
                )
            }
        },
        text = {
            val scrollModifier = Modifier.verticalScroll(rememberScrollState())
            if (isEditing) {
                MetadataForm(draft, metadata.isEnabled, { draft = it }, scrollModifier)
            } else {
                MetadataSummary(metadata, scrollModifier)
            }
        },
        confirmButton = {
            if (isEditing) {
                TextButton(onClick = {
                    onSave(metadata.withFieldsFrom(draft))
                    if (isGoLive) onGoLive() else isEditing = false
                }) { Text(if (isGoLive) "Go live" else "Save", color = InfoBlue) }
            } else {
                TextButton(onClick = {
                    draft = metadata
                    isEditing = true
                }) { Text("Edit", color = InfoBlue) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEditing) "Cancel" else "Close", color = InfoBlue) }
        }
    )
}

@Composable
fun StopBroadcastDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stop broadcast?") },
        text = { Text("Your viewers will lose the stream.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Stop broadcast", color = ErrorRed) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep live") } }
    )
}
