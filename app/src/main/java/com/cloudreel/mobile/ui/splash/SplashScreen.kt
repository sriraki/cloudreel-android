package com.cloudreel.mobile.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.ui.components.CloudReelEmblem

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navTarget by viewModel.navigationTarget.collectAsState()

    // 1. Symbol scale & alpha animation
    val symbolScale = remember { Animatable(0.75f) }
    val symbolAlpha = remember { Animatable(0f) }

    // 2. Wordmark alpha animation
    val wordmarkAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.checkSessionAndProceed()

        // Step 1: Reveal the CloudReel emblem with subtle scale and fade
        symbolAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        symbolScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Step 2: Smoothly reveal wordmark
        wordmarkAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(navTarget) {
        when (navTarget) {
            is SplashNavigationTarget.GoLiveHome -> onNavigateToHome()
            is SplashNavigationTarget.Login -> onNavigateToLogin()
            is SplashNavigationTarget.Idle -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated CloudReel Symbol
            CloudReelEmblem(
                modifier = Modifier
                    .size(86.dp)
                    .scale(symbolScale.value)
                    .alpha(symbolAlpha.value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Animated CloudReel Wordmark
            Text(
                text = "CloudReel",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.alpha(wordmarkAlpha.value)
            )
        }
    }
}
