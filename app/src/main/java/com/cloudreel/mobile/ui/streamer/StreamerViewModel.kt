package com.cloudreel.mobile.ui.streamer

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudreel.mobile.data.local.SessionManager
import com.cloudreel.mobile.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/** What Settings keeps between launches. All fields defaulted so Gson can load older saves. */
internal data class SavedSettings(
    val videoFormat: VideoFormat = VideoFormat.P1080_30,
    val latencySeconds: Float = 2f,
    val filePrefix: String = "CR",
    val metadata: StreamMetadata = StreamMetadata(),
    val activeLinks: List<LinkKind> = DEMO_LINKS.filter { it.isActive }.map { it.kind },
    val settings: StreamerSettings = StreamerSettings()
)

/**
 * State holder for the Streamer flow (camera screen, recordings, settings).
 *
 * Live mode is a UI demo: connecting and bitrate are simulated so the
 * flow can be shown before the backend ingest exists. Store mode records real video.
 */
class StreamerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StreamerUiState())
    val uiState: StateFlow<StreamerUiState> = _uiState.asStateFlow()

    private val recordingsDir: File =
        (application.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: application.filesDir)
            .apply { mkdirs() }

    private val uploadPrefs =
        application.getSharedPreferences(UPLOAD_PREFS_NAME, Context.MODE_PRIVATE)

    private val settingsPrefs =
        application.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var lastSavedSettings: SavedSettings? = null

    /** Signed-in user for Settings > Account & Plan, null when signed out. */
    val signedInUser: User? = SessionManager(application).getUser()

    private var tickerJob: Job? = null

    init {
        restoreSettings()
        refreshRecordings()
    }

    fun update(transform: (StreamerUiState) -> StreamerUiState) {
        _uiState.update(transform)
        saveSettingsIfChanged()
    }

    // ── Saved settings ────────────────────────────────────────────────────────

    private fun restoreSettings() {
        val saved = settingsPrefs.getString(KEY_SETTINGS, null)
            ?.let { json -> runCatching { gson.fromJson(json, SavedSettings::class.java) }.getOrNull() }
            ?: return
        lastSavedSettings = saved
        _uiState.update { state ->
            state.copy(
                videoFormat = saved.videoFormat,
                latencySeconds = saved.latencySeconds,
                filePrefix = saved.filePrefix,
                metadata = saved.metadata,
                settings = saved.settings,
                links = state.links.map { link -> link.copy(isActive = link.kind in saved.activeLinks) }
            )
        }
    }

    private fun saveSettingsIfChanged() {
        val state = _uiState.value
        val current = SavedSettings(
            videoFormat = state.videoFormat,
            latencySeconds = state.latencySeconds,
            filePrefix = state.filePrefix,
            metadata = state.metadata,
            activeLinks = state.links.filter { it.isActive }.map { it.kind },
            settings = state.settings
        )
        if (current == lastSavedSettings) return
        lastSavedSettings = current
        settingsPrefs.edit().putString(KEY_SETTINGS, gson.toJson(current)).apply()
    }

    fun showMessage(message: String) = _uiState.update { it.copy(message = message) }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    // ── Live (simulated) ──────────────────────────────────────────────────────

    fun startLive() {
        _uiState.update { it.copy(phase = StreamPhase.CONNECTING, elapsedSeconds = 0) }
        viewModelScope.launch {
            delay(CONNECT_DELAY_MS)
            if (_uiState.value.phase == StreamPhase.CONNECTING) {
                _uiState.update { it.copy(phase = StreamPhase.LIVE) }
                startTicker()
            }
        }
    }

    fun stopSession() {
        tickerJob?.cancel()
        _uiState.update {
            it.copy(phase = StreamPhase.IDLE, elapsedSeconds = 0)
        }
    }

    // ── Meeting ───────────────────────────────────────────────────────────────

    // Meetings face the speaker, so they open on the front camera.
    fun startHostSession(sessionCode: String) {
        if (_uiState.value.isMeeting) return
        _uiState.update {
            it.copy(
                isHostSession = true,
                sessionCode = sessionCode,
                callStartedAtMillis = System.currentTimeMillis(),
                isFrontCamera = true,
                guests = SAMPLE_GUESTS,
                waitingGuests = SAMPLE_WAITING_GUESTS
            )
        }
    }

    fun startGuestSession(sessionCode: String) {
        if (_uiState.value.isMeeting) return
        _uiState.update {
            it.copy(
                isGuestSession = true,
                sessionCode = sessionCode,
                callStartedAtMillis = System.currentTimeMillis(),
                isFrontCamera = true,
                guests = SAMPLE_GUEST_VIEW
            )
        }
    }

    // Admitted guests join the gallery straight away.
    fun admitGuest(guest: Guest) = _uiState.update { it.copy(waitingGuests = it.waitingGuests - guest, guests = it.guests + guest) }

    fun admitAllGuests() = _uiState.update { it.copy(waitingGuests = emptyList(), guests = it.guests + it.waitingGuests) }

    fun denyGuest(guest: Guest) = _uiState.update { it.copy(waitingGuests = it.waitingGuests - guest) }

    fun removeGuest(guest: Guest) = _uiState.update { state -> state.copy(guests = state.guests.filter { it.id != guest.id }) }

    fun toggleGuestMute(guest: Guest) = _uiState.update { state ->
        state.copy(guests = state.guests.map { if (it.id == guest.id) it.copy(isMuted = !it.isMuted) else it })
    }

    // shortcut: the link is built on the phone; the session server should issue it once it exists.
    fun createInvite(guestName: String, quality: String, isJoiningDirectly: Boolean) = _uiState.update { state ->
        val code = state.sessionCode.ifBlank { "CR-SESSION" }
        val token = (1000..9999).random()
        val url = "https://cloudreel.app/join/${Uri.encode(code)}?guest=$token"
        state.copy(invites = listOf(InviteLink(guestName, quality, isJoiningDirectly, url)) + state.invites)
    }

    // ── Store (real recording, driven by camera events) ───────────────────────

    fun newRecordingFile(): File {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        if (_uiState.value.settings.filenameTime == FilenameTime.UTC) format.timeZone = TimeZone.getTimeZone("UTC")
        val stamp = format.format(Date())
        return File(recordingsDir, "${_uiState.value.filePrefix}_$stamp.mp4")
    }

    fun onRecordingStarted() {
        _uiState.update { it.copy(phase = StreamPhase.RECORDING, elapsedSeconds = 0) }
        startTicker()
    }

    fun setPaused(isPaused: Boolean) {
        _uiState.update {
            it.copy(
                phase = when {
                    isPaused -> StreamPhase.PAUSED
                    it.mode == StreamMode.LIVE -> StreamPhase.LIVE
                    else -> StreamPhase.RECORDING
                }
            )
        }
    }

    fun onRecordingFinished(errorMessage: String?) {
        stopSession()
        refreshRecordings()
        showMessage(errorMessage ?: "Recording saved. Open the cloud icon to upload.")
    }

    // ── Recordings library ────────────────────────────────────────────────────

    fun refreshRecordings() {
        val uploadedNames = uploadPrefs.getStringSet(KEY_UPLOADED, emptySet()).orEmpty()
        val items = recordingsDir.listFiles { file -> file.extension == "mp4" }
            .orEmpty()
            .sortedByDescending { it.lastModified() }
            .map { RecordingItem(it, it.length(), it.name in uploadedNames) }
        _uiState.update { it.copy(recordings = items) }
    }

    fun deleteRecordings(files: Collection<File>) {
        files.forEach { it.delete() }
        val remaining = uploadPrefs.getStringSet(KEY_UPLOADED, emptySet()).orEmpty() -
            files.map { it.name }.toSet()
        uploadPrefs.edit().putStringSet(KEY_UPLOADED, remaining).apply()
        refreshRecordings()
    }

    /**
     * shortcut: simulated upload, swap the loop for the MAM path used by the web app:
     * POST v1/pcr/presigned-url (AssetType "MAMPrimary") then PUT the file to the returned url.
     */
    fun uploadRecordings(files: Collection<File>) {
        viewModelScope.launch {
            files.filter { it.name !in _uiState.value.uploadProgress }.forEach { file ->
                for (step in 1..UPLOAD_STEPS) {
                    _uiState.update {
                        it.copy(uploadProgress = it.uploadProgress + (file.name to step / UPLOAD_STEPS.toFloat()))
                    }
                    delay(UPLOAD_STEP_MS)
                }
                val uploaded = uploadPrefs.getStringSet(KEY_UPLOADED, emptySet()).orEmpty() + file.name
                uploadPrefs.edit().putStringSet(KEY_UPLOADED, uploaded).apply()
                _uiState.update { it.copy(uploadProgress = it.uploadProgress - file.name) }
                refreshRecordings()
            }
        }
    }

    // ── Clock (timer, quota, simulated link speeds) ───────────────────────────

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    when (state.phase) {
                        StreamPhase.LIVE -> state.copy(
                            elapsedSeconds = state.elapsedSeconds + 1,
                            timeLeftSeconds = (state.timeLeftSeconds - 1).coerceAtLeast(0),
                            links = state.links.map(::jitterLink)
                        )
                        StreamPhase.RECORDING -> state.copy(
                            elapsedSeconds = state.elapsedSeconds + 1,
                            timeLeftSeconds = (state.timeLeftSeconds - 1).coerceAtLeast(0)
                        )
                        else -> state
                    }
                }
            }
        }
    }

    private fun jitterLink(link: LinkStatus): LinkStatus =
        if (link.isActive && link.kind != LinkKind.INTERNET) {
            link.copy(megabitsPerSecond = (link.megabitsPerSecond + Random.nextFloat() * 2f - 1f).coerceIn(0.5f, 60f))
        } else {
            link
        }

    private companion object {
        const val UPLOAD_PREFS_NAME = "cloudreel_uploads"
        const val SETTINGS_PREFS_NAME = "cloudreel_streamer_settings"
        const val KEY_SETTINGS = "settings_json"
        const val KEY_UPLOADED = "uploaded_names"
        const val CONNECT_DELAY_MS = 1500L
        const val UPLOAD_STEPS = 10
        const val UPLOAD_STEP_MS = 250L
    }
}
