package com.cloudreel.mobile.ui.streamer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.cloudreel.mobile.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Captures the phone's screen while the host shares it. Android only allows screen capture from a
 * foreground service of type mediaProjection, which shows a notification while it runs.
 */
class ScreenShareService : Service() {

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val permissionData = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_PERMISSION_DATA, Intent::class.java) }
        if (permissionData == null || projection != null) return START_NOT_STICKY

        // Android 14 needs the service in the foreground before it hands out the projection.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        )
        val mediaProjection = getSystemService(MediaProjectionManager::class.java)
            .getMediaProjection(intent.getIntExtra(EXTRA_RESULT_CODE, 0), permissionData)
        if (mediaProjection == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        // The user can also stop sharing from the system's status bar chip.
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() = stopSelf()
        }, Handler(Looper.getMainLooper()))

        val metrics = resources.displayMetrics
        // shortcut: frames are captured and dropped; send them to the session's media server once it exists.
        val reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
        reader.setOnImageAvailableListener({ it.acquireLatestImage()?.close() }, Handler(Looper.getMainLooper()))
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "CloudReel screen share",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null
        )
        projection = mediaProjection
        imageReader = reader
        sharingState.value = true
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        virtualDisplay?.release()
        imageReader?.close()
        projection?.stop()
        sharingState.value = false
        super.onDestroy()
    }

    private fun buildNotification() = run {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Screen sharing", NotificationManager.IMPORTANCE_LOW))
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_streamer_camera)
            .setContentTitle("You're sharing your screen")
            .setContentText("Return to CloudReel to stop sharing.")
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "screen_share"
        private const val NOTIFICATION_ID = 7
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_PERMISSION_DATA = "permission_data"

        private val sharingState = MutableStateFlow(false)
        val isSharing: StateFlow<Boolean> = sharingState.asStateFlow()

        /** [resultCode] and [permissionData] come from the system's "Start sharing?" prompt. */
        fun start(context: Context, resultCode: Int, permissionData: Intent) {
            val intent = Intent(context, ScreenShareService::class.java)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_PERMISSION_DATA, permissionData)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenShareService::class.java))
        }
    }
}
