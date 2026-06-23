import SwiftUI

struct VerificationRequiredView: View {
    let onBack: () -> Void
    let onUseDifferentAccess: () -> Void

    var body: some View {
        AccessEntryFormView(
            title: "Verification required",
            subtitle: "Accessing requires identity verification before this mobile session can continue.",
            primaryActionTitle: "Check again",
            secondaryActionTitle: "Use different access",
            onPrimaryAction: onBack,
            onSecondaryAction: onUseDifferentAccess,
            onBack: onBack,
            statusMessage: nil
        ) {
            Text("Verification is owned by the backend. Mobiling only routes this state.")
        }
    }
}
