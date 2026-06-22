import SwiftUI

struct SecondFactorRequiredView: View {
    let onBack: () -> Void
    let onUseDifferentAccess: () -> Void

    var body: some View {
        AccessEntryFormView(
            title: "Second factor required",
            subtitle: "Accessing requires an additional verification step before this mobile session can continue.",
            primaryActionTitle: "Check again",
            secondaryActionTitle: "Use different access",
            onPrimaryAction: onBack,
            onSecondaryAction: onUseDifferentAccess,
            onBack: onBack,
            statusMessage: nil
        ) {
            Text("Second-factor validation is owned by the backend. Mobiling only routes this state.")
        }
    }
}
