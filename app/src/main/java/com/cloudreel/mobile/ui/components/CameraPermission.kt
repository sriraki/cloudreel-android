package com.cloudreel.mobile.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

class CameraPermissionState(
    val isGranted: Boolean,
    /** True after the user said no; Android may stop showing the prompt, so offer the settings page. */
    val wasDenied: Boolean,
    val request: () -> Unit,
    val openAppSettings: () -> Unit
)

/** Camera + microphone permission: asks when first shown and re-checks when the app resumes (e.g. back from Settings). */
@Composable
fun rememberCameraPermission(): CameraPermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun hasAllPermissions() = CAMERA_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    var isGranted by remember { mutableStateOf(hasAllPermissions()) }
    var wasDenied by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        isGranted = result.values.all { it }
        wasDenied = !isGranted
    }

    LaunchedEffect(Unit) {
        if (!isGranted) launcher.launch(CAMERA_PERMISSIONS)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) isGranted = hasAllPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return CameraPermissionState(
        isGranted = isGranted,
        wasDenied = wasDenied,
        request = { launcher.launch(CAMERA_PERMISSIONS) },
        openAppSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            )
        }
    )
}
