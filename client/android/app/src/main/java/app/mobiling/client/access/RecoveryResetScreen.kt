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
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import kotlinx.coroutines.launch

@Composable
fun RecoveryResetScreen(
    onBack: () -> Unit,
    onRequestRecovery: () -> Unit,
    onResetRecovery: suspend (AccessResetRecoveryRequest) -> AccessAuthSessionPayload? = { null },
    onAccessSession: (AccessAuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AccessEntryFormScaffold(
        title = "Reset access",
        subtitle = "Use your recovery code and choose a new password.",
        primaryActionLabel = "Reset access",
        secondaryActionLabel = "Request code",
        onPrimaryAction = {
            coroutineScope.launch {
                status = null
                try {
                    val payload = onResetRecovery(
                        AccessResetRecoveryRequest(
                            email = email,
                            code = code,
                            password = password,
                        ),
                    )
                    if (payload == null) {
                        status = AccessUnavailableMessage
                    } else {
                        onAccessSession(payload)
                    }
                } catch (_: Exception) {
                    status = "Recovery reset could not be completed."
                }
            }
        },
        onSecondaryAction = onRequestRecovery,
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
            value = code,
            onValueChange = { code = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Recovery code") },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New password") },
            singleLine = true,
        )
    }
}
