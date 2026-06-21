import SwiftUI

struct SignInView: View {
    let onBack: () -> Void
    let onCreateAccess: () -> Void
    @State private var email = ""
    @State private var password = ""
    @State private var statusMessage: String?

    var body: some View {
        AccessEntryFormView(
            title: "Sign in",
            subtitle: "Use your SmartResponsor access to enter the business workspace.",
            primaryActionTitle: "Sign in",
            secondaryActionTitle: "Create access",
            onPrimaryAction: { statusMessage = accessUnavailableMessage },
            onSecondaryAction: onCreateAccess,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                TextField("Email", text: $email)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .textFieldStyle(.roundedBorder)
                SecureField("Password", text: $password)
                    .textFieldStyle(.roundedBorder)
            }
        }
    }
}
