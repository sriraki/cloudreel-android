package com.cloudreel.mobile.ui.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import com.cloudreel.mobile.R

/** Plays the CloudReel logo reveal (res/raw/splash_reveal.mp4, with sound), then moves on. */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToHome: () -> Unit
) {
    val navTarget by viewModel.navigationTarget.collectAsState()

    LaunchedEffect(Unit) { viewModel.startFallbackTimer() }

    LaunchedEffect(navTarget) {
        when (navTarget) {
            is SplashNavigationTarget.Home -> onNavigateToHome()
            is SplashNavigationTarget.Idle -> Unit
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.cr_splash_background))
            .clipToBounds()
            .semantics { contentDescription = "CloudReel" },
        contentAlignment = Alignment.Center
    ) {
        // Fill the screen like a centre crop: scale the 9:16 video until it covers both sides. The video keeps
        // the logo small in the middle of a plain background, so on tall phones the wordmark is about half the width.
        val unit = maxOf(maxWidth / VIDEO_WIDTH_RATIO, maxHeight / VIDEO_HEIGHT_RATIO)
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.splash_reveal}"))
                    setOnCompletionListener { viewModel.onRevealFinished() }
                    setOnErrorListener { _, _, _ ->
                        viewModel.onRevealFinished()
                        true
                    }
                    start()
                }
            },
            modifier = Modifier.requiredSize(unit * VIDEO_WIDTH_RATIO, unit * VIDEO_HEIGHT_RATIO)
        )
    }
}

private const val VIDEO_WIDTH_RATIO = 9f
private const val VIDEO_HEIGHT_RATIO = 16f
