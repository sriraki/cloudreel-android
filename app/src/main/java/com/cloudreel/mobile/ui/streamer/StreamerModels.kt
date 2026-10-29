package com.cloudreel.mobile.ui.streamer

import android.hardware.camera2.CameraMetadata
import androidx.annotation.DrawableRes
import com.cloudreel.mobile.R
import java.io.File
import java.util.Locale

enum class StreamMode(val label: String) {
    LIVE("Live"),
    STORE("Store")
}

enum class StreamPhase {
    IDLE,
    CONNECTING,
    LIVE,
    RECORDING,
    PAUSED
}

enum class WhiteBalance(val label: String, val awbMode: Int) {
    AUTO("Auto", CameraMetadata.CONTROL_AWB_MODE_AUTO),
    DAYLIGHT("Daylight", CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT),
    CLOUDY("Cloudy", CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT),
    TUNGSTEN("Tungsten", CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT),
    FLUORESCENT("Fluorescent", CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT)
}

/**
 * Formats in the camera's format picker. The phone records progressive video only, so interlaced
 * and SD formats ([isNative] false) capture the nearest progressive size and are saved as a label
 * for the server until ingest converts them. [captureFps] null means auto frame rate.
 */
enum class VideoFormat(
    val label: String,
    val rateText: String,
    val captureHeight: Int,
    val captureFps: Int?,
    val isNative: Boolean
) {
    P1080_60("1080p", "60", 1080, 60, true),
    P1080_50("1080p", "50", 1080, 50, true),
    P1080_30("1080p", "30", 1080, 30, true),
    I1080_60("1080i", "60", 1080, 30, false),
    I1080_50("1080i", "50", 1080, 25, false),
    P720_60("720p", "60", 720, 60, true),
    P720_50("720p", "50", 720, 50, true),
    P720_30("720p", "30", 720, 30, true),
    I576_25("576i", "25", 480, 25, false),
    I576_AUTO("576i", "Auto", 480, null, false),
    I480_30("480i", "30", 480, 30, false),
    I480_AUTO("480i", "Auto", 480, null, false);

    /** Text inside the camera icon: the rate, blank for 25/30, "A" for auto. */
    val badge: String
        get() = when (rateText) {
            "Auto" -> "A"
            "25", "30" -> ""
            else -> rateText
        }

    val displayName: String
        get() = "$label $rateText"
}

enum class LinkKind(val label: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Cellular"),
    INTERNET("Internet"),
    WIRED("Wired")
}

enum class AudioChannels(val label: String) {
    MONO("Mono"),
    STEREO("Stereo")
}

enum class LocationSharing(val label: String) {
    NEVER("Never"),
    ALWAYS("Always"),
    WHILE_RUNNING("While app is running")
}

enum class PreviewOrientation(val label: String) {
    AUTO("Auto"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class PreviewMirroring(val label: String) {
    OFF("Off"),
    HORIZONTAL("Horizontally"),
    VERTICAL("Vertically")
}

enum class FilenameTime(val label: String) {
    LOCAL("Local time"),
    UTC("UTC+0")
}

enum class LogSeverity(val label: String) {
    ERROR("Error"),
    WARNING("Warning"),
    INFO("Info"),
    VERBOSE("Verbose")
}

/**
 * Settings pages beyond video quality and metadata. Every field has a default so settings
 * saved by an older build still load (Gson uses the no-argument constructor).
 */
data class StreamerSettings(
    val channels: AudioChannels = AudioChannels.STEREO,
    val isIfbEnabled: Boolean = true,
    val isIfbLowDelay: Boolean = true,
    val ifbVolume: Int = 10,
    val isCbrEncoding: Boolean = false,
    val isBondingEnabled: Boolean = true,
    val totalBandwidthKbps: Int = 8000,
    val cellularBandwidthKbps: Int = 12000,
    val wifiBandwidthKbps: Int = 12000,
    val locationSharing: LocationSharing = LocationSharing.WHILE_RUNNING,
    val serverUrl: String = "",
    val previewOrientation: PreviewOrientation = PreviewOrientation.AUTO,
    val previewMirroring: PreviewMirroring = PreviewMirroring.OFF,
    val filenameTime: FilenameTime = FilenameTime.LOCAL,
    val isDoubleTapHidesControls: Boolean = true,
    val isAudioPassThrough: Boolean = false,
    val isTallyLight: Boolean = false,
    val logSeverity: LogSeverity = LogSeverity.INFO
)

val LATENCY_OPTIONS_SECONDS = listOf(0.5f, 1f, 2f, 5f)

// shortcut: fixed demo quota, replace with the plan's remaining time from the backend
const val DEMO_QUOTA_SECONDS = 2 * 60 * 60

data class StreamMetadata(
    val isEnabled: Boolean = true,
    val isPromptOnStart: Boolean = false,
    val slugline: String = "",
    val crewName: String = "",
    val storyId: String = "",
    val usageTerms: String = "",
    val provider: String = "",
    val reporter: String = "",
    val keywords: String = "",
    val comments: String = ""
) {
    fun hasContent(): Boolean =
        listOf(slugline, crewName, storyId, usageTerms, provider, reporter, keywords, comments).any { it.isNotBlank() }
}

data class LinkStatus(
    val kind: LinkKind,
    val isActive: Boolean,
    val megabitsPerSecond: Float
)

data class RecordingItem(
    val file: File,
    val sizeBytes: Long,
    val isUploaded: Boolean
)

/** A tile's place in the meeting frame, as fractions of the frame's width and height. */
data class FrameCell(val x: Float, val y: Float, val width: Float, val height: Float)

/**
 * Gallery tiles for [count] people, as in Teams or Zoom: an even grid that grows with the count and centres
 * the last row. Portrait (a tall screen) uses fewer columns, landscape (16:9) more.
 */
fun galleryCells(count: Int, isLandscape: Boolean): List<FrameCell> {
    val columns = if (isLandscape) {
        when {
            count <= 1 -> 1
            count <= 4 -> 2
            count <= 9 -> 3
            else -> 4
        }
    } else {
        when {
            count <= 2 -> 1
            count <= 6 -> 2
            else -> 3
        }
    }
    val rows = (count + columns - 1) / columns
    val lastRowCount = count - columns * (rows - 1)
    return List(count) { index ->
        val row = index / columns
        val rowOffset = if (row == rows - 1) (columns - lastRowCount) / (2f * columns) else 0f
        FrameCell(rowOffset + (index % columns) / columns.toFloat(), row / rows.toFloat(), 1f / columns, 1f / rows)
    }
}

/** Who opened the camera screen: a reporter going live, or a host or guest in a meeting. */
enum class StreamerRole { REPORTER, HOST, GUEST }

data class Guest(
    val id: Int,
    val name: String,
    @DrawableRes val photo: Int? = null,
    val isMuted: Boolean = false
) {
    val initials: String
        get() = name.split(' ').mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
}

data class InviteLink(
    val guestName: String,
    val quality: String,
    val isJoiningDirectly: Boolean,
    val url: String
)

// shortcut: sample people stand in for the session server's participant list; replace when the backend exists.
val SAMPLE_GUESTS = listOf(
    Guest(1, "Priya Sharma", R.drawable.category_guest_1),
    Guest(2, "Arjun Rao", R.drawable.category_guest_2, isMuted = true)
)
val SAMPLE_WAITING_GUESTS = listOf(
    Guest(3, "Ravi Kumar", R.drawable.category_host),
    Guest(4, "Meera Iyer"),
    Guest(5, "Sameer Khan"),
    Guest(6, "Lakshmi Nair"),
    Guest(7, "Vikram Singh"),
    Guest(8, "Anjali Desai"),
    Guest(9, "Rahul Menon"),
    Guest(10, "Deepa Pillai")
)

/** What a guest sees once admitted: the host and the other guests. */
val SAMPLE_GUEST_VIEW = listOf(Guest(11, "Ravi Kumar (Host)", R.drawable.category_host)) + SAMPLE_GUESTS

val INVITE_QUALITIES = listOf("1080p", "720p", "480p", "360p", "240p", "180p", "120p")

data class StreamerUiState(
    val mode: StreamMode = StreamMode.LIVE,
    val phase: StreamPhase = StreamPhase.IDLE,
    val elapsedSeconds: Int = 0,
    val timeLeftSeconds: Int = DEMO_QUOTA_SECONDS,
    val videoFormat: VideoFormat = VideoFormat.P1080_30,
    val latencySeconds: Float = 2f,
    val isMicMuted: Boolean = false,
    val isFlashOn: Boolean = false,
    val isGridOn: Boolean = false,
    val isAutoFocus: Boolean = true,
    val isExposureLocked: Boolean = false,
    val exposureIndex: Int = 0,
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val zoomRatio: Float = 1f,
    val isFrontCamera: Boolean = false,
    val isToolsOpen: Boolean = false,
    val metadata: StreamMetadata = StreamMetadata(),
    val settings: StreamerSettings = StreamerSettings(),
    val links: List<LinkStatus> = DEMO_LINKS,
    val audioLevel: Float = 0f,
    val filePrefix: String = "CR",
    val recordings: List<RecordingItem> = emptyList(),
    val uploadProgress: Map<String, Float> = emptyMap(),
    val message: String? = null,
    // ── Meeting (the camera opened from Start as Host, or by a guest the host admitted) ──
    val isHostSession: Boolean = false,
    val isGuestSession: Boolean = false,
    val sessionCode: String = "",
    val callStartedAtMillis: Long = 0L,
    val isCameraOff: Boolean = false,
    // Everyone else in the call, in the order they joined; the gallery shows them all after "You".
    val guests: List<Guest> = emptyList(),
    val waitingGuests: List<Guest> = emptyList(),
    // Newest first.
    val invites: List<InviteLink> = emptyList()
) {
    val isSessionActive: Boolean
        get() = phase != StreamPhase.IDLE

    val isMeeting: Boolean
        get() = isHostSession || isGuestSession
}

val DEMO_LINKS = listOf(
    LinkStatus(LinkKind.WIFI, isActive = true, megabitsPerSecond = 18.4f),
    LinkStatus(LinkKind.CELLULAR, isActive = true, megabitsPerSecond = 24.1f),
    LinkStatus(LinkKind.INTERNET, isActive = true, megabitsPerSecond = 0f),
    LinkStatus(LinkKind.WIRED, isActive = false, megabitsPerSecond = 0f)
)

fun formatTimer(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

/** Call length as a meeting app shows it: 05:12, or 1:05:12 after an hour. */
fun formatCallTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds) else String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun formatTimeLeft(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    return if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
}

fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> String.format(Locale.US, "%.1f MB", bytes / 1_000_000.0)
    else -> String.format(Locale.US, "%.0f KB", bytes / 1_000.0)
}
