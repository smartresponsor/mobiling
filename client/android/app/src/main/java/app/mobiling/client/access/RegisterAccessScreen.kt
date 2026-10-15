package app.mobiling.client.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import app.mobiling.client.design.MobileDesignDefaults
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.design.MobileDesignSystem
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterAccessScreen(
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onRegisterAccess: suspend (AccessRegisterAuthRequest) -> AccessAuthSessionPayload? = { null },
    onAccessSession: (AccessAuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AccessFlowShell(
        title = "Create access",
        subtitle = "Set up a guest entry for the 1tasker workspace.",
        styledSubtitle = buildAnnotatedString {
            append("Set up a guest entry for the ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("1tasker") }
            append(" workspace.")
        },
        primaryActionLabel = "Create access",
        secondaryActionLabel = "Sign in instead",
        onPrimaryAction = {
            if (password.length < 8) {
                status = "Password must contain at least 8 characters."
            } else if (password != confirmPassword) {
                status = "Passwords do not match."
            } else {
                coroutineScope.launch {
                    status = null
                    try {
                        val displayName = email.substringBefore('@').ifBlank { "Guest" }
                        val payload = onRegisterAccess(
                            AccessRegisterAuthRequest(
                                displayName = displayName,
                                email = email,
                                password = password,
                                deviceLabel = "Android",
                            ),
                        )
                        if (payload == null) status = AccessUnavailableMessage else onAccessSession(payload)
                    } catch (_: Exception) {
                        status = "Access could not be created."
                    }
                }
            }
        },
        onSecondaryAction = onSignIn,
        onBack = onBack,
        status = status,
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
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
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide confirmed password" else "Show confirmed password",
                    )
                }
            },
        )
        PasswordQualityHint(password = password, confirmPassword = confirmPassword)
    }
}

@Composable
private fun PasswordQualityHint(password: String, confirmPassword: String) {
    val checks = listOf(
        "At least 8 characters" to (password.length >= 8),
        "Uppercase letter" to password.any(Char::isUpperCase),
        "Lowercase letter" to password.any(Char::isLowerCase),
        "Number" to password.any(Char::isDigit),
        "Symbol" to password.any { !it.isLetterOrDigit() },
    )
    val score = checks.count { it.second }
    val quality = when (score) {
        0, 1 -> "Weak"
        2, 3 -> "Fair"
        4 -> "Good"
        else -> "Strong"
    }

    Column(verticalArrangement = Arrangement.spacedBy(MobileDesignDefaults.Access.passwordQualityGap)) {
        Text("Password quality: $quality", style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(
            progress = { score / checks.size.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        checks.forEach { (label, satisfied) ->
            Row(horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm)) {
                Icon(
                    imageVector = if (satisfied) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (satisfied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (confirmPassword.isNotEmpty()) {
            PasswordRequirementRow("Passwords match", password == confirmPassword)
        }
        Text(
            "Required: at least 8 characters. Other checks improve password strength.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasswordRequirementRow(label: String, satisfied: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm)) {
        Icon(
            imageVector = if (satisfied) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (satisfied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}