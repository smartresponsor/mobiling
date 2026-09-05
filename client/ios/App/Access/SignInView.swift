import SwiftUI

struct SignInView: View {
    let onBack: () -> Void
    let onCreateAccess: () -> Void
    let onRecoverAccess: () -> Void
    let onStartAccess: (StartAuthRequest) async throws -> AuthSessionPayload?
    let onAccessSession: (AuthSessionPayload) -> Void
    @State private var email = ""
    @State private var password = ""
    @State private var passwordVisible = false
    @State private var statusMessage: String?
    @State private var emailError: String?
    @State private var passwordError: String?
    @FocusState private var focusedField: Field?

    private enum Field {
        case email
        case password
    }

    var body: some View {
        AccessFlowShellView(
            title: "Sign in",
            subtitle: "Use your SmartResponsor access to enter the business workspace.",
            primaryActionTitle: "Sign in",
            secondaryActionTitle: "Recover access",
            onPrimaryAction: {
                let normalizedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
                emailError = normalizedEmail.isEmpty
                    ? "Enter your email address."
                    : (normalizedEmail.contains("@") ? nil : "Enter a valid email address.")
                passwordError = password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    ? "Enter your password."
                    : nil

                if emailError != nil || passwordError != nil {
                    statusMessage = "Check the highlighted fields and try again."
                    focusedField = emailError != nil ? .email : .password
                    return
                }

                Task {
                    statusMessage = nil
                    do {
                        let payload = try await onStartAccess(
                            StartAuthRequest(
                                login: normalizedEmail,
                                password: password,
                                deviceLabel: "iOS"
                            )
                        )
                        if let payload {
                            onAccessSession(payload)
                        } else {
                            statusMessage = accessUnavailableMessage
                        }
                    } catch {
                        statusMessage = "We couldn't sign you in. Check your connection and try again."
                    }
                }
            },
            onSecondaryAction: onRecoverAccess,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    TextField("Email", text: $email)
                        .textInputAutocapitalization(.never)
                        .keyboardType(.emailAddress)
                        .textFieldStyle(.roundedBorder)
                        .focused($focusedField, equals: .email)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(emailError == nil ? Color.clear : Color.red, lineWidth: 1)
                        )
                        .onChange(of: email) { _ in
                            if emailError != nil { emailError = nil }
                            if statusMessage == "Check the highlighted fields and try again." { statusMessage = nil }
                        }
                    if let emailError {
                        Text(emailError)
                            .font(.caption)
                            .foregroundStyle(.red)
                    }
                }
                VStack(alignment: .leading, spacing: 4) {
                    AccessPasswordField(
                        title: "Password",
                        text: $password,
                        isVisible: $passwordVisible,
                        isError: passwordError != nil
                    )
                    .focused($focusedField, equals: .password)
                    .onChange(of: password) { _ in
                        if passwordError != nil { passwordError = nil }
                        if statusMessage == "Check the highlighted fields and try again." { statusMessage = nil }
                    }
                    if let passwordError {
                        Text(passwordError)
                            .font(.caption)
                            .foregroundStyle(.red)
                    }
                }
            }
        }
    }
}