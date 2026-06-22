package com.smartresponsor.mobile.app.access

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VerificationRequiredScreen(
    onBack: () -> Unit,
    onUseDifferentAccess: () -> Unit,
) {
    AccessEntryFormScaffold(
        title = "Verification required",
        subtitle = "Accessing requires identity verification before this mobile session can continue.",
        primaryActionLabel = "Check again",
        secondaryActionLabel = "Use different access",
        onPrimaryAction = onBack,
        onSecondaryAction = onUseDifferentAccess,
        onBack = onBack,
    ) {
        Text("Verification is owned by the backend. Mobiling only routes this state.")
