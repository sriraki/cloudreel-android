package com.cloudreel.mobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudreel.mobile.data.local.SessionManager
import com.cloudreel.mobile.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NetworkBondingState(
    val cellular5GConnected: Boolean = true,
    val wifiConnected: Boolean = true,
    val bondedSpeedMbps: Float = 142.5f,
    val latencyMs: Int = 18,
    val packetLossPercent: Float = 0.0f
)

data class HomeUiState(
    val user: User? = null,
    val bondingState: NetworkBondingState = NetworkBondingState(),
    val selectedRole: Role? = null
)

enum class Role {
    HOST,
    PARTICIPANT,
    STREAMER,
    RELAY
}

class HomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserSession()
    }

    private fun loadUserSession() {
        viewModelScope.launch {
            val user = sessionManager.getUser()
            _uiState.value = _uiState.value.copy(user = user)
        }
    }

    fun selectRole(role: Role) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
