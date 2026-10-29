package com.cloudreel.mobile.ui.streamer

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.theme.SuccessGreen
import java.io.File

/** Local recordings made in Store mode. Upload sends a file to the MAM only when the user taps it. */
@Composable
fun RecordingsScreen(
    viewModel: StreamerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var selectedFiles by remember { mutableStateOf(setOf<File>()) }
    var filesPendingDelete by remember { mutableStateOf<Set<File>?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshRecordings() }

    val recordings = state.recordings
    val usedBytes = recordings.sumOf { it.sizeBytes }
    val freeBytes = recordings.firstOrNull()?.file?.parentFile?.usableSpace

    fun share(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share recording"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f)) {
                Text("Recordings", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = buildString {
                        append("${formatFileSize(usedBytes)} used")
                        if (freeBytes != null) append(" · ${formatFileSize(freeBytes)} free")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (recordings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "No recordings yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        // Bulk bar: select all, upload, delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selectedFiles.size == recordings.size,
                onCheckedChange = { isChecked ->
                    selectedFiles = if (isChecked) recordings.map { it.file }.toSet() else emptySet()
                }
            )
            Text(
                text = if (selectedFiles.isEmpty()) "${recordings.size} files" else "${selectedFiles.size} selected",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                enabled = selectedFiles.isNotEmpty(),
                onClick = {
                    viewModel.uploadRecordings(selectedFiles)
                    selectedFiles = emptySet()
                }
            ) { Icon(Icons.Filled.Upload, contentDescription = "Upload selected") }
            IconButton(
                enabled = selectedFiles.isNotEmpty(),
                onClick = { filesPendingDelete = selectedFiles }
            ) { Icon(Icons.Filled.Delete, contentDescription = "Delete selected") }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(recordings, key = { it.file.name }) { item ->
                val progress = state.uploadProgress[item.file.name]
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.file in selectedFiles,
                            onCheckedChange = { isChecked ->
                                selectedFiles = if (isChecked) selectedFiles + item.file else selectedFiles - item.file
                            }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(item.file.nameWithoutExtension, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = formatFileSize(item.sizeBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when {
                                        progress != null -> "Uploading ${(progress * 100).toInt()}%"
                                        item.isUploaded -> "Uploaded"
                                        else -> "Not uploaded"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.isUploaded) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            enabled = !item.isUploaded && progress == null,
                            onClick = { viewModel.uploadRecordings(listOf(item.file)) }
                        ) { Icon(Icons.Filled.Upload, contentDescription = "Upload ${item.file.nameWithoutExtension}") }
                        IconButton(onClick = { share(item.file) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share ${item.file.nameWithoutExtension}")
                        }
                        IconButton(onClick = { filesPendingDelete = setOf(item.file) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete ${item.file.nameWithoutExtension}", tint = ErrorRed)
                        }
                    }
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    filesPendingDelete?.let { files ->
        AlertDialog(
            onDismissRequest = { filesPendingDelete = null },
            title = { Text(if (files.size == 1) "Delete recording?" else "Delete ${files.size} recordings?") },
            text = { Text("They are removed from this phone and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecordings(files)
                    selectedFiles = selectedFiles - files
                    filesPendingDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { filesPendingDelete = null }) { Text("Cancel") } }
        )
    }
}
