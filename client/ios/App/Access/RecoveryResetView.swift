import SwiftUI

struct RecoveryResetView: View {
    let onBack: () -> Void
    let onRequestRecovery: () -> Void
    let onResetRecovery: (ResetRecoveryRequest) async throws -> AuthSessionPayload?
    let onAccessSession: (AuthSessionPayload) -> Void
    @State private var code = ""
    @State private var password = ""
    @State private var statusMessage: String?

    var body: some View {
        AccessEntryFormView(
            title: "Reset access",
            subtitle: "Use your recovery code and choose a new password.",
            primaryActionTitle: "Reset access",
            secondaryActionTitle: "Request code",
            onPrimaryAction: {
                Task {
                    statusMessage = nil
                    do {
                        let payload = try await onResetRecovery(
                            ResetRecoveryRequest(
                                code: code,
                                password: password
                            )
                        )
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
                TextField("Recovery code", text: $code)
                    .textInputAutocapitalization(.never)
                    .textFieldStyle(.roundedBorder)
                SecureField("New password", text: $password)
                    .textFieldStyle(.roundedBorder)
            }
        }
    }
}