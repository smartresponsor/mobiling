import SwiftUI

struct RecoveryResetView: View {
    let onBack: () -> Void
    let onRequestRecovery: () -> Void
    let onResetRecovery: (ResetRecoveryRequest) async throws -> AuthSessionPayload?
    let onAccessSession: (AuthSessionPayload) -> Void
    @State private var email = ""
    @State private var code = ""
    @State private var password = ""
    @State private var statusMessage: String?

    var body: some View {
        AccessFlowShellView(
            title: "Reset access",
            subtitle: "Use your recovery code and choose a new password.",
            primaryActionTitle: "Reset access",
            secondaryActionTitle: "Request code",
            onPrimaryAction: {
                Task {
                    statusMessage = nil
                    do {
                        var request = ResetRecoveryRequest(
                            code: code,
                            password: password
                        )
                        request.email = email

                        let payload = try await onResetRecovery(request)

                        if let payload {
                            onAccessSession(payload)
                        } else {
                            statusMessage = accessUnavailableMessage
                        }
                    } catch {
                        statusMessage = "Recovery reset could not be completed."
                    }
                }
            },
            onSecondaryAction: onRequestRecovery,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                TextField("Email", text: $email)
                    .textInputAutocapitalization(.never)
                    .textFieldStyle(.roundedBorder)
                TextField("Recovery code", text: $code)
                    .textInputAutocapitalization(.never)
                    .textFieldStyle(.roundedBorder)
                SecureField("New password", text: $password)
                    .textFieldStyle(.roundedBorder)
            }
        }
    }
}