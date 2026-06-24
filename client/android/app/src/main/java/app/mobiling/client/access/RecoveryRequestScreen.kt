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
import app.mobiling.client.contract.auth.session.RequestRecoveryRequest
import kotlinx.coroutines.launch

@Composable
fun RecoveryRequestScreen(
    onBack: () -> Unit,
    onHaveRecoveryCode: () -> Unit,
    onRequestRecovery: suspend (RequestRecoveryRequest) -> AuthSessionPayload? = { null },
    onAccessSession: (AuthSessionPayload) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AccessEntryFormScaffold(
        title = "Recover access",
        subtitle = "Request a recovery code for your SmartResponsor access.",
        primaryActionLabel = "Send recovery code",
        secondaryActionLabel = "I have a recovery code",
        onPrimaryAction = {
            coroutineScope.launch {
                status = null
                try {
                    val payload = onRequestRecovery(RequestRecoveryRequest(email = email))
                    if (payload == null) {
                        status = AccessUnavailableMessage
                    } else {
                        onAccessSession(payload)
                    }
                } catch (_: Exception) {
                    status = "Recovery request could not be started."
                }
            }
        },
        onSecondaryAction = onHaveRecoveryCode,
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
