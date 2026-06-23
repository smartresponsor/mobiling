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
import app.mobiling.client.contract.auth.session.StartAuthRequest
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onCreateAccess: () -> Unit,
    onStartAccess: suspend (StartAuthRequest) -> AuthSessionPayload? = { null },
    onAccessSession: (AuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AccessEntryFormScaffold(
        title = "Sign in",
        subtitle = "Use your SmartResponsor access to enter the business workspace.",
        primaryActionLabel = "Sign in",
        secondaryActionLabel = "Create access",
        onPrimaryAction = {
            coroutineScope.launch {
                status = null
                try {
                    val payload = onStartAccess(
                        StartAuthRequest(
                            login = email,
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
                    status = "Access session could not be started."
                }
            }
        },
        onSecondaryAction = onCreateAccess,
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
        )
    }
}
