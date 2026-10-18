package com.cloudreel.mobile.ui.auth

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.theme.CRMagenta
import com.cloudreel.mobile.theme.ErrorRed
import com.cloudreel.mobile.ui.components.CloudReelHeader
import com.cloudreel.mobile.ui.components.CloudReelInputField
import com.cloudreel.mobile.ui.components.EnterprisePlatformBadge
import com.cloudreel.mobile.ui.components.GradientButton

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.signUpState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            if (event is AuthNavigationEvent.NavigateToHome) {
                onNavigateToHome()
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
            // Header with back button and theme toggle
            CloudReelHeader(
                showBackButton = true,
                onBackClick = onNavigateToLogin
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                EnterprisePlatformBadge()

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.signup_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.signup_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (!state.generalError.isNullOrBlank()) {
                    Text(
                        text = state.generalError.orEmpty(),
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Full Name
                CloudReelInputField(
                    value = state.fullName,
                    onValueChange = viewModel::onSignUpFullNameChanged,
                    label = stringResource(R.string.fullname_label),
                    placeholder = stringResource(R.string.fullname_placeholder),
                    leadingIcon = Icons.Default.Person,
                    errorMessage = state.fullNameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Work Email
                CloudReelInputField(
                    value = state.email,
                    onValueChange = viewModel::onSignUpEmailChanged,
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

                Spacer(modifier = Modifier.height(18.dp))

                // Password
                CloudReelInputField(
                    value = state.password,
                    onValueChange = viewModel::onSignUpPasswordChanged,
                    label = stringResource(R.string.password_label),
                    placeholder = stringResource(R.string.password_placeholder),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = state.isPasswordVisible,
                    onTogglePasswordVisibility = viewModel::toggleSignUpPasswordVisibility,
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm Password
                CloudReelInputField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onSignUpConfirmPasswordChanged,
                    label = stringResource(R.string.confirm_password_label),
                    placeholder = stringResource(R.string.confirm_password_placeholder),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = state.isPasswordVisible,
                    onTogglePasswordVisibility = viewModel::toggleSignUpPasswordVisibility,
                    errorMessage = state.confirmPasswordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.signUp()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))

                GradientButton(
                    text = stringResource(R.string.create_account),
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.signUp()
                    },
                    isLoading = state.isLoading,
                    showTrailingArrow = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.already_have_account),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = stringResource(R.string.sign_in_link),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CRMagenta,
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
