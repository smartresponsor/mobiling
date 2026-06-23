package app.mobiling.client.access

import androidx.compose.foundation.layout.fillMaxWidth
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
import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterAccessScreen(
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onRegisterAccess: suspend (RegisterAuthRequest) -> AuthSessionPayload? = { null },
    onAccessSession: (AuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var companyName by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AccessEntryFormScaffold(
        title = "Create access",
        subtitle = "Set up a guest entry for the SmartResponsor workspace.",
        primaryActionLabel = "Create access",
        secondaryActionLabel = "Sign in instead",
        onPrimaryAction = {
            coroutineScope.launch {
                status = null
                try {
                    val payload = onRegisterAccess(
                        RegisterAuthRequest(
                            displayName = companyName,
                            email = email,
                            password = password,
                            deviceLabel = "Android",
                        ),
                    )
                    if (payload == null) {
                        status = AccessUnavailableMessage
                    } else {
                        onAccessSession(payload)
                    }
                } catch (_: Exception) {
                    status = "Access could not be created."
                }
            }
        },
        onSecondaryAction = onSignIn,
        onBack = onBack,
        status = status,
    ) {
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Company name") },
            singleLine = true,
        )
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
        )
    }
}