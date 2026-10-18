package com.cloudreel.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cloudreel.mobile.navigation.CloudReelNavGraph
import com.cloudreel.mobile.theme.CloudReelTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ SplashScreen API
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by rememberSaveable { mutableStateOf(systemDark) }

            CloudReelTheme(
                darkTheme = isDarkMode,
                onToggleTheme = { isDarkMode = !isDarkMode }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val app = application as CloudReelApp
                    CloudReelNavGraph(
                        sessionManager = app.sessionManager,
                        authRepository = app.authRepository
                    )
                }
            }
        }
    }
}
