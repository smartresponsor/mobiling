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
fun RegisterAccessScreen(
    onBack: () -> Unit,
    onSignIn: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var companyName by rememberSaveable { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    AccessEntryFormScaffold(
        title = "Create access",
        subtitle = "Set up a guest entry for the SmartResponsor workspace.",
        primaryActionLabel = "Create access",
        secondaryActionLabel = "Sign in instead",
        onPrimaryAction = { status = AccessUnavailableMessage },
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
