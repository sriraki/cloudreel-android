package com.cloudreel.mobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.CloudReelInputField
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge
import com.cloudreel.mobile.ui.components.GradientButton

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToHome -> onNavigateToHome()
                is AuthNavigationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with CloudReel logo and theme toggle
            CloudReelHeader()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Enterprise Platform Badge matching Screenshot 2
                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(18.dp))

                // Heading
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // General Error Banner if authentication fails
                if (!state.generalError.isNullOrBlank()) {
                    Text(
                        text = state.generalError.orEmpty(),
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Work Email Field
                CloudReelInputField(
                    value = state.email,
                    onValueChange = viewModel::onLoginEmailChanged,
                    label = stringResource(R.string.email_label),
                    placeholder = stringResource(R.string.email_placeholder),
                    leadingIcon = Icons.Default.Mail,
                    errorMessage = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password Field
                CloudReelInputField(
                    value = state.password,
                    onValueChange = viewModel::onLoginPasswordChanged,
                    label = stringResource(R.string.password_label),
                    placeholder = stringResource(R.string.password_placeholder),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = state.isPasswordVisible,
                    onTogglePasswordVisibility = viewModel::toggleLoginPasswordVisibility,
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.signIn()
                        }
                    )
                )

                // Forgot Password Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password),
                        color = CRViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showForgotDialog = true }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Primary Sign In Button with Purple/Magenta Gradient
                GradientButton(
                    text = stringResource(R.string.sign_in_button),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.signIn()
                    },
                    isLoading = state.isLoading,
                    showTrailingArrow = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Create Account Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dont_have_account),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = stringResource(R.string.create_account),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CRMagenta,
                        modifier = Modifier
                            .clickable { onNavigateToSignUp() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text(
                    text = "Password Recovery",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Enter your work email address (${state.email.ifBlank { "e.g. name@company.com" }}) to receive password reset instructions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotDialog = false
                        viewModel.requestPasswordReset(state.email.ifBlank { "your work email" })
                    }
                ) {
                    Text("Send Instructions", color = CRViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
