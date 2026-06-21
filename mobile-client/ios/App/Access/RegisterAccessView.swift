import SwiftUI

struct RegisterAccessView: View {
    let onBack: () -> Void
    let onSignIn: () -> Void
    @State private var companyName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var statusMessage: String?

    var body: some View {
        AccessEntryFormView(
            title: "Create access",
            subtitle: "Set up a guest entry for the SmartResponsor workspace.",
            primaryActionTitle: "Create access",
            secondaryActionTitle: "Sign in instead",
            onPrimaryAction: { statusMessage = accessUnavailableMessage },
            onSecondaryAction: onSignIn,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                TextField("Company name", text: $companyName)
                    .textFieldStyle(.roundedBorder)
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
