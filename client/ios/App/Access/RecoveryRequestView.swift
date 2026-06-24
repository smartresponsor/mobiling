import SwiftUI

struct RecoveryRequestView: View {
    let onBack: () -> Void
    let onHaveRecoveryCode: () -> Void
    let onRequestRecovery: (RequestRecoveryRequest) async throws -> AuthSessionPayload?
    let onAccessSession: (AuthSessionPayload) -> Void
    @State private var email = ""
    @State private var statusMessage: String?

    var body: some View {
        AccessEntryFormView(
            title: "Recover access",
            subtitle: "Request a recovery code for your SmartResponsor access.",
            primaryActionTitle: "Send recovery code",
            secondaryActionTitle: "I have a recovery code",
            onPrimaryAction: {
                Task {
                    statusMessage = nil
                    do {
                        let payload = try await onRequestRecovery(RequestRecoveryRequest(email: email))
                        if let payload {
                            onAccessSession(payload)
                        } else {
                            statusMessage = accessUnavailableMessage
                        }
                    } catch {
                        statusMessage = "Recovery request could not be started."
                    }
                }
            },
            onSecondaryAction: onHaveRecoveryCode,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                TextField("Email", text: $email)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .textFieldStyle(.roundedBorder)
            }
        }
    }
}