package app.mobiling.client.access

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.testTag
import app.mobiling.client.BuildConfig
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onCreateAccess: () -> Unit,
    onRecoverAccess: () -> Unit,
    onStartAccess: suspend (AccessStartAuthRequest) -> AccessAuthSessionPayload? = { null },
    onAccessSession: (AccessAuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf(BuildConfig.DEBUG_LOGIN) }
    var password by rememberSaveable { mutableStateOf(BuildConfig.DEBUG_PASSWORD) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    AccessFlowShell(
        title = "Sign in",
        subtitle = "Use your 1tasker access to enter the business workspace.",
        styledSubtitle = buildAnnotatedString {
            append("Use your ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("1tasker")
            }
            append(" access to enter the business workspace.")
        },
        primaryActionLabel = "Sign in",
        primaryActionLoading = isSubmitting,
        secondaryActionLabel = "Recover access",
        onPrimaryAction = {
            val normalizedEmail = email.trim()
            emailError = when {
                normalizedEmail.isEmpty() -> "Enter your email address."
                !normalizedEmail.contains('@') -> "Enter a valid email address."
                else -> null
            }
            passwordError = if (password.isBlank()) "Enter your password." else null

            if (emailError != null || passwordError != null) {
                status = "Check the highlighted fields and try again."
                when {
                    emailError != null -> emailFocusRequester.requestFocus()
                    passwordError != null -> passwordFocusRequester.requestFocus()
                }
            } else if (!isSubmitting) {
                coroutineScope.launch {
                    isSubmitting = true
                    status = null
                    try {
                        val payload = onStartAccess(
                            AccessStartAuthRequest(
                                login = normalizedEmail,
                                password = password,
                                deviceLabel = "Android",
                            ),
                        )
                        when {
                            payload == null -> status = AccessUnavailableMessage
                            payload.authenticated || payload.requiresVerification || payload.requiresSecondFactor -> {
                                onAccessSession(payload)
                            }
                            else -> {
                                status = payload.status
                                    .takeIf { it.isNotBlank() && it != "unauthenticated" }
                                    ?: "The email or password is incorrect."
                            }
                        }
                    } catch (error: Exception) {
                        status = error.message?.takeIf { it.isNotBlank() }
                            ?: "We couldn't sign you in. Check your connection and try again."
                    } finally {
                        isSubmitting = false
                    }
                }
            }
        },
        onSecondaryAction = onRecoverAccess,
        onBack = onBack,
        status = status,
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) emailError = null
                if (status == "Check the highlighted fields and try again.") status = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocusRequester)
                .testTag("access-sign-in-email"),
            label = { Text("Email") },
            singleLine = true,
            isError = emailError != null,
            supportingText = emailError?.let { message -> { Text(message) } },
        )
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null
                if (status == "Check the highlighted fields and try again.") status = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
                .testTag("access-sign-in-password"),
            label = { Text("Password") },
            singleLine = true,
            isError = passwordError != null,
            supportingText = passwordError?.let { message -> { Text(message) } },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
        )
    }
}
