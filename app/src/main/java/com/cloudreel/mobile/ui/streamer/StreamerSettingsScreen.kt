package com.cloudreel.mobile.ui.streamer

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.data.model.User
import com.cloudreel.mobile.theme.InfoBlue

private enum class SettingsPage(val title: String, val parent: SettingsPage? = null) {
    LIST("Settings"),
    SESSION("Session Settings", LIST),
    METADATA("Metadata", LIST),
    NETWORK("Network Settings", LIST),
    ADVANCED("Advanced", LIST),
    LOGS("System Logs", ADVANCED),
    ACCOUNT("Account & Plan", LIST),
    ABOUT("About", LIST)
}

/** Which popup is open; one at a time across all pages. */
private enum class SettingsDialog {
    VIDEO_FORMAT, LATENCY, CHANNELS, IFB_VOLUME,
    TOTAL_BANDWIDTH, CELLULAR_BANDWIDTH, WIFI_BANDWIDTH, LOCATION, SERVER_URL,
    ORIENTATION, MIRRORING, FILENAME_TIME, FILE_PREFIX, LOG_SEVERITY
}

private const val MAX_FILE_PREFIX_LENGTH = 12
private val BANDWIDTH_RANGE_KBPS = 500..20000
private const val BANDWIDTH_STEP_KBPS = 500

private typealias SettingsUpdate = ((StreamerSettings) -> StreamerSettings) -> Unit

@Composable
fun StreamerSettingsScreen(
    viewModel: StreamerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var pageName by rememberSaveable { mutableStateOf(SettingsPage.LIST.name) }
    var dialogName by rememberSaveable { mutableStateOf<String?>(null) }
    val page = SettingsPage.valueOf(pageName)
    val openDialog: (SettingsDialog) -> Unit = { dialogName = it.name }
    val updateSettings: SettingsUpdate = { transform -> viewModel.update { it.copy(settings = transform(it.settings)) } }
    val goBack = { page.parent?.let { pageName = it.name } ?: onNavigateBack() }

    BackHandler(enabled = page != SettingsPage.LIST) { goBack() }

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
            IconButton(onClick = goBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(page.title, style = MaterialTheme.typography.titleLarge)
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            when (page) {
                SettingsPage.LIST -> SettingsPage.entries.filter { it.parent == SettingsPage.LIST }.forEach { entry ->
                    SettingsRow(title = entry.title) { pageName = entry.name }
                }
                SettingsPage.SESSION -> SessionPage(state, updateSettings, openDialog)
                SettingsPage.METADATA -> MetadataPage(state.metadata) { updated -> viewModel.update { it.copy(metadata = updated) } }
                SettingsPage.NETWORK -> NetworkPage(state, viewModel::update, updateSettings, openDialog)
                SettingsPage.ADVANCED -> AdvancedPage(state, updateSettings, openDialog) { pageName = SettingsPage.LOGS.name }
                SettingsPage.LOGS -> LogsPage(state.settings, openDialog)
                SettingsPage.ACCOUNT -> AccountPage(viewModel.signedInUser, state.timeLeftSeconds)
                SettingsPage.ABOUT -> AboutPage()
            }
        }
    }

    dialogName?.let { name ->
        SettingsDialogHost(SettingsDialog.valueOf(name), state, viewModel::update, updateSettings) { dialogName = null }
    }
}

@Composable
private fun SessionPage(state: StreamerUiState, updateSettings: SettingsUpdate, openDialog: (SettingsDialog) -> Unit) {
    val settings = state.settings
    SettingsSection("Video")
    SettingsRow(
        "Video format", state.videoFormat.displayName,
        note = if (state.videoFormat.isNative) null else NOT_ACTIVE_NOTE
    ) { openDialog(SettingsDialog.VIDEO_FORMAT) }
    SettingsRow("Video delay", formatSeconds(state.latencySeconds)) { openDialog(SettingsDialog.LATENCY) }
    SettingsSwitchRow("CBR encoding", settings.isCbrEncoding, { isOn -> updateSettings { it.copy(isCbrEncoding = isOn) } },
        note = NOT_ACTIVE_NOTE)
    SettingsSection("Audio")
    SettingsRow("Channels", settings.channels.label, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.CHANNELS) }
    SettingsSection("Return audio (IFB)")
    SettingsSwitchRow("IFB", settings.isIfbEnabled, { isOn -> updateSettings { it.copy(isIfbEnabled = isOn) } }, note = NOT_ACTIVE_NOTE)
    SettingsSwitchRow("IFB low delay", settings.isIfbLowDelay, { isOn -> updateSettings { it.copy(isIfbLowDelay = isOn) } }, note = NOT_ACTIVE_NOTE)
    SettingsRow("IFB volume", "${settings.ifbVolume}", NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.IFB_VOLUME) }
}

@Composable
private fun MetadataPage(metadata: StreamMetadata, onChange: (StreamMetadata) -> Unit) {
    SettingsSwitchRow(
        title = "Ask before going live",
        isChecked = metadata.isPromptOnStart,
        onCheckedChange = { onChange(metadata.copy(isPromptOnStart = it)) },
        value = "Show this form each time you tap Go live"
    )
    SettingsSwitchRow(
        title = "Send with the live stream",
        isChecked = metadata.isEnabled,
        onCheckedChange = { onChange(metadata.copy(isEnabled = it)) }
    )
    SettingsSection("Fields")
    // Same flow as the tag popup: read-only until Edit, changes stay a draft until Save.
    var isEditing by remember { mutableStateOf(!metadata.hasContent()) }
    var draft by remember { mutableStateOf(metadata) }
    if (isEditing) {
        MetadataForm(draft, metadata.isEnabled, { draft = it }, Modifier.padding(vertical = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { isEditing = false }) { Text("Cancel", color = InfoBlue) }
            Button(
                onClick = {
                    onChange(metadata.withFieldsFrom(draft))
                    isEditing = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
            ) { Text("Save") }
        }
    } else {
        MetadataSummary(metadata, Modifier.padding(vertical = 12.dp))
        OutlinedButton(onClick = {
            draft = metadata
            isEditing = true
        }) { Text("Edit", color = InfoBlue) }
    }
}

@Composable
private fun NetworkPage(
    state: StreamerUiState,
    update: ((StreamerUiState) -> StreamerUiState) -> Unit,
    updateSettings: SettingsUpdate,
    openDialog: (SettingsDialog) -> Unit
) {
    val settings = state.settings
    SettingsSection("Connections")
    state.links.filter { it.kind != LinkKind.INTERNET }.forEach { link ->
        SettingsSwitchRow(link.kind.label, link.isActive, { isOn ->
            update { current ->
                current.copy(links = current.links.map { if (it.kind == link.kind) it.copy(isActive = isOn) else it })
            }
        })
    }
    SettingsSwitchRow("Bonding", settings.isBondingEnabled, { isOn -> updateSettings { it.copy(isBondingEnabled = isOn) } },
        value = "Combine connections into one stream", note = NOT_ACTIVE_NOTE)
    SettingsSection("Bandwidth limits")
    SettingsRow("Total", "${settings.totalBandwidthKbps} kbps", NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.TOTAL_BANDWIDTH) }
    SettingsRow("Cellular", "${settings.cellularBandwidthKbps} kbps", NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.CELLULAR_BANDWIDTH) }
    SettingsRow("Wi-Fi", "${settings.wifiBandwidthKbps} kbps", NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.WIFI_BANDWIDTH) }
    SettingsSection("Other")
    SettingsRow("Location sharing", settings.locationSharing.label, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.LOCATION) }
    SettingsRow("Server", settings.serverUrl.ifBlank { "Default" }, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.SERVER_URL) }
}

@Composable
private fun AdvancedPage(
    state: StreamerUiState,
    updateSettings: SettingsUpdate,
    openDialog: (SettingsDialog) -> Unit,
    onOpenLogs: () -> Unit
) {
    val settings = state.settings
    SettingsSection("Preview")
    SettingsRow("Orientation", settings.previewOrientation.label) { openDialog(SettingsDialog.ORIENTATION) }
    SettingsRow("Mirroring", settings.previewMirroring.label) { openDialog(SettingsDialog.MIRRORING) }
    SettingsSwitchRow("Double tap hides controls", settings.isDoubleTapHidesControls,
        { isOn -> updateSettings { it.copy(isDoubleTapHidesControls = isOn) } })
    SettingsSection("Recordings")
    SettingsRow("File prefix", state.filePrefix, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.FILE_PREFIX) }
    SettingsRow("Time in file names", settings.filenameTime.label, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.FILENAME_TIME) }
    SettingsSection("Device")
    SettingsSwitchRow("Audio pass-through", settings.isAudioPassThrough,
        { isOn -> updateSettings { it.copy(isAudioPassThrough = isOn) } }, note = NOT_ACTIVE_NOTE)
    SettingsSwitchRow("Tally light", settings.isTallyLight,
        { isOn -> updateSettings { it.copy(isTallyLight = isOn) } }, value = "Flash light while live", note = NOT_ACTIVE_NOTE)
    SettingsRow("System logs", onClick = onOpenLogs)
}

@Composable
private fun LogsPage(settings: StreamerSettings, openDialog: (SettingsDialog) -> Unit) {
    SettingsRow("Severity", settings.logSeverity.label, NOT_ACTIVE_NOTE) { openDialog(SettingsDialog.LOG_SEVERITY) }
    // shortcut: no log store yet, wire these when logging to file lands
    SettingsRow("Clear logs", note = NOT_ACTIVE_NOTE) {}
    SettingsRow("Report an issue", note = NOT_ACTIVE_NOTE) {}
}

@Composable
private fun AccountPage(user: User?, timeLeftSeconds: Int) {
    SettingsSection("Account")
    InfoLine("Signed in as", user?.username ?: "Not signed in")
    user?.email?.let { InfoLine("Email", it) }
    user?.organization?.let { InfoLine("Organization", it) }
    SettingsSection("Plan")
    InfoLine("Streaming time", formatTimeLeft(timeLeftSeconds))
    SettingsSection("This device")
    InfoLine("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
}

@SuppressLint("HardwareIds")
@Composable
private fun AboutPage() {
    val context = LocalContext.current
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    InfoLine("App", "CloudReel Streamer")
    InfoLine("Version", versionName ?: "-")
    InfoLine("Device ID", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "-")
}

@Composable
private fun InfoLine(title: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatSeconds(seconds: Float) = "${seconds.toString().removeSuffix(".0")} sec"

@Composable
private fun SettingsDialogHost(
    dialog: SettingsDialog,
    state: StreamerUiState,
    update: ((StreamerUiState) -> StreamerUiState) -> Unit,
    updateSettings: SettingsUpdate,
    onDismiss: () -> Unit
) {
    val settings = state.settings
    when (dialog) {
        SettingsDialog.VIDEO_FORMAT -> ChoiceDialog("Video format", VideoFormat.entries, state.videoFormat, { it.displayName },
            { choice -> update { it.copy(videoFormat = choice) } }, onDismiss)
        SettingsDialog.LATENCY -> ChoiceDialog("Video delay", LATENCY_OPTIONS_SECONDS, state.latencySeconds, ::formatSeconds,
            { choice -> update { it.copy(latencySeconds = choice) } }, onDismiss)
        SettingsDialog.CHANNELS -> ChoiceDialog("Channels", AudioChannels.entries, settings.channels, { it.label },
            { choice -> updateSettings { it.copy(channels = choice) } }, onDismiss)
        SettingsDialog.IFB_VOLUME -> NumberDialog("IFB volume", settings.ifbVolume, 0..10, 1, "",
            { value -> updateSettings { it.copy(ifbVolume = value) } }, onDismiss)
        SettingsDialog.TOTAL_BANDWIDTH -> NumberDialog("Total bandwidth", settings.totalBandwidthKbps, BANDWIDTH_RANGE_KBPS,
            BANDWIDTH_STEP_KBPS, "kbps", { value -> updateSettings { it.copy(totalBandwidthKbps = value) } }, onDismiss)
        SettingsDialog.CELLULAR_BANDWIDTH -> NumberDialog("Cellular bandwidth", settings.cellularBandwidthKbps, BANDWIDTH_RANGE_KBPS,
            BANDWIDTH_STEP_KBPS, "kbps", { value -> updateSettings { it.copy(cellularBandwidthKbps = value) } }, onDismiss)
        SettingsDialog.WIFI_BANDWIDTH -> NumberDialog("Wi-Fi bandwidth", settings.wifiBandwidthKbps, BANDWIDTH_RANGE_KBPS,
            BANDWIDTH_STEP_KBPS, "kbps", { value -> updateSettings { it.copy(wifiBandwidthKbps = value) } }, onDismiss)
        SettingsDialog.LOCATION -> ChoiceDialog("Location sharing", LocationSharing.entries, settings.locationSharing, { it.label },
            { choice -> updateSettings { it.copy(locationSharing = choice) } }, onDismiss)
        SettingsDialog.SERVER_URL -> TextDialog("Server", settings.serverUrl,
            { value -> updateSettings { it.copy(serverUrl = value) } }, onDismiss)
        SettingsDialog.ORIENTATION -> ChoiceDialog("Orientation", PreviewOrientation.entries, settings.previewOrientation, { it.label },
            { choice -> updateSettings { it.copy(previewOrientation = choice) } }, onDismiss)
        SettingsDialog.MIRRORING -> ChoiceDialog("Mirroring", PreviewMirroring.entries, settings.previewMirroring, { it.label },
            { choice -> updateSettings { it.copy(previewMirroring = choice) } }, onDismiss)
        SettingsDialog.FILENAME_TIME -> ChoiceDialog("Time in file names", FilenameTime.entries, settings.filenameTime, { it.label },
            { choice -> updateSettings { it.copy(filenameTime = choice) } }, onDismiss)
        SettingsDialog.FILE_PREFIX -> TextDialog("File prefix", state.filePrefix, { typed ->
            val cleaned = typed.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(MAX_FILE_PREFIX_LENGTH)
            if (cleaned.isNotEmpty()) update { it.copy(filePrefix = cleaned) }
        }, onDismiss)
        SettingsDialog.LOG_SEVERITY -> ChoiceDialog("Severity", LogSeverity.entries, settings.logSeverity, { it.label },
            { choice -> updateSettings { it.copy(logSeverity = choice) } }, onDismiss)
    }
}
