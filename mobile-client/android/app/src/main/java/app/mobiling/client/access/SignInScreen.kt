package app.mobiling.client.access

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    AccessEntryFormScaffold(
        title = "Sign in",
        subtitle = "Use your SmartResponsor access to enter the business workspace.",
        primaryActionLabel = "Sign in",
        secondaryActionLabel = "Create access",
        onPrimaryAction = { status = AccessUnavailableMessage },
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
