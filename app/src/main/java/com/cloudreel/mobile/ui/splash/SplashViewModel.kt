package com.cloudreel.mobile.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudreel.mobile.data.local.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SplashNavigationTarget {
    object Idle : SplashNavigationTarget()
    object Home : SplashNavigationTarget()
}

class SplashViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _navigationTarget = MutableStateFlow<SplashNavigationTarget>(SplashNavigationTarget.Idle)
    val navigationTarget: StateFlow<SplashNavigationTarget> = _navigationTarget.asStateFlow()

    /** The logo reveal video ended or could not play. */
    fun onRevealFinished() {
        _navigationTarget.value = SplashNavigationTarget.Home
    }

    /** Safety net in case the video never reports its end, e.g. the app was sent to the background mid-reveal. */
    fun startFallbackTimer() {
        viewModelScope.launch {
            delay(MAX_SPLASH_MS)
            onRevealFinished()
        }
    }

    private companion object {
        // The reveal is 4.75 s; allow a little for the player to start.
        const val MAX_SPLASH_MS = 7000L
    }
}
