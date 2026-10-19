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

    fun checkSessionAndProceed() {
        viewModelScope.launch {
            // Keep brand animation smooth (~1.4 seconds)
            delay(1400)
            _navigationTarget.value = SplashNavigationTarget.Home
        }
    }
}
