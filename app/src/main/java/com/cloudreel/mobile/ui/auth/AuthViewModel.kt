package com.cloudreel.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudreel.mobile.data.model.AuthResult
import com.cloudreel.mobile.data.remote.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

data class SignUpUiState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

sealed class AuthNavigationEvent {
    object NavigateToHome : AuthNavigationEvent()
    data class ShowToast(val message: String) : AuthNavigationEvent()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _navEvents = MutableSharedFlow<AuthNavigationEvent>()
    val navEvents: SharedFlow<AuthNavigationEvent> = _navEvents.asSharedFlow()

    // ── Login Handlers ────────────────────────────────────────────────────────

    fun onLoginEmailChanged(email: String) {
        _loginState.value = _loginState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onLoginPasswordChanged(password: String) {
        _loginState.value = _loginState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.value = _loginState.value.copy(
            isPasswordVisible = !_loginState.value.isPasswordVisible
        )
    }

    fun signIn() {
        val email = _loginState.value.email.trim()
        val password = _loginState.value.password

        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null

        if (email.isBlank()) {
            emailErr = "Work email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Please enter a valid email address"
            hasError = true
        }

        if (password.isBlank()) {
            passErr = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }

        if (hasError) {
            _loginState.value = _loginState.value.copy(
                emailError = emailErr,
                passwordError = passErr
            )
            return
        }

        _loginState.value = _loginState.value.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _loginState.value = _loginState.value.copy(isLoading = false)
                    _navEvents.emit(AuthNavigationEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        generalError = result.message
                    )
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    // ── Sign Up Handlers ──────────────────────────────────────────────────────

    fun onSignUpFullNameChanged(name: String) {
        _signUpState.value = _signUpState.value.copy(fullName = name, fullNameError = null)
    }

    fun onSignUpEmailChanged(email: String) {
        _signUpState.value = _signUpState.value.copy(email = email, emailError = null)
    }

    fun onSignUpPasswordChanged(password: String) {
        _signUpState.value = _signUpState.value.copy(password = password, passwordError = null)
    }

    fun onSignUpConfirmPasswordChanged(confirm: String) {
        _signUpState.value = _signUpState.value.copy(confirmPassword = confirm, confirmPasswordError = null)
    }

    fun toggleSignUpPasswordVisibility() {
        _signUpState.value = _signUpState.value.copy(
            isPasswordVisible = !_signUpState.value.isPasswordVisible
        )
    }

    fun signUp() {
        val fullName = _signUpState.value.fullName.trim()
        val email = _signUpState.value.email.trim()
        val password = _signUpState.value.password
        val confirm = _signUpState.value.confirmPassword

        var hasError = false
        var nameErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null

        if (fullName.isBlank()) {
            nameErr = "Full name is required"
            hasError = true
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Valid work email is required"
            hasError = true
        }
        if (password.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }
        if (password != confirm) {
            confirmErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _signUpState.value = _signUpState.value.copy(
                fullNameError = nameErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr
            )
            return
        }

        _signUpState.value = _signUpState.value.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            when (val result = authRepository.signup(fullName, email, password)) {
                is AuthResult.Success -> {
                    _signUpState.value = _signUpState.value.copy(isLoading = false)
                    _navEvents.emit(AuthNavigationEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _signUpState.value = _signUpState.value.copy(
                        isLoading = false,
                        generalError = result.message
                    )
                }
                AuthResult.Loading -> Unit
            }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _navEvents.emit(
                AuthNavigationEvent.ShowToast(
                    "Password recovery link sent to $email (check corporate inbox)"
                )
            )
        }
    }
}
