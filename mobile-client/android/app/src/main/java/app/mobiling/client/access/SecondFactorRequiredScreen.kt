package app.mobiling.client.access

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SecondFactorRequiredScreen(
    onBack: () -> Unit,
    onUseDifferentAccess: () -> Unit,
) {
    AccessEntryFormScaffold(
        title = "Second factor required",
        subtitle = "Accessing requires an additional verification step before this mobile session can continue.",
        primaryActionLabel = "Check again",
        secondaryActionLabel = "Use different access",
        onPrimaryAction = onBack,
        onSecondaryAction = onUseDifferentAccess,
        onBack = onBack,
        status = null,
    ) {
        Text("Second-factor validation is owned by the backend. Mobiling only routes this state.")

    }
}
