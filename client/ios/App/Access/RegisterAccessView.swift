import SwiftUI

struct RegisterAccessView: View {
    let onBack: () -> Void
    let onSignIn: () -> Void
    let onRegisterAccess: (RegisterAuthRequest) async throws -> AuthSessionPayload?
    let onAccessSession: (AuthSessionPayload) -> Void

    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var passwordVisible = false
    @State private var confirmPasswordVisible = false
    @State private var statusMessage: String?

    var body: some View {
        AccessFlowShellView(
            title: "Create access",
            subtitle: "Set up a guest entry for the 1tasker workspace.",
            primaryActionTitle: "Create access",
            secondaryActionTitle: "Sign in instead",
            onPrimaryAction: {
                guard password.count >= 8 else {
                    statusMessage = "Password must contain at least 8 characters."
                    return
                }
                guard password == confirmPassword else {
                    statusMessage = "Passwords do not match."
                    return
                }

                Task {
                    statusMessage = nil
                    do {
                        let localPart = email.split(separator: "@", maxSplits: 1).first.map(String.init) ?? ""
                        let payload = try await onRegisterAccess(
                            RegisterAuthRequest(
                                displayName: localPart.isEmpty ? "Guest" : localPart,
                                email: email,
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
                        statusMessage = "Access could not be created."
                    }
                }
            },
            onSecondaryAction: onSignIn,
            onBack: onBack,
            statusMessage: statusMessage
        ) {
            VStack(alignment: .leading, spacing: 12) {
                TextField("Email", text: $email)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .textFieldStyle(.roundedBorder)
                AccessPasswordField(
                    title: "Password",
                    text: $password,
                    isVisible: $passwordVisible
                )
                PasswordQualityHint(
                    password: password,
                    confirmPassword: confirmPassword
                )
                AccessPasswordField(
                    title: "Confirm password",
                    text: $confirmPassword,
                    isVisible: $confirmPasswordVisible
                )
            }
        }
    }
}

private struct PasswordQualityHint: View {
    let password: String
    let confirmPassword: String

    private var checks: [(String, Bool)] {
        [
            ("At least 8 characters", password.count >= 8),
            ("Uppercase letter", password.contains(where: { $0.isUppercase })),
            ("Lowercase letter", password.contains(where: { $0.isLowercase })),
            ("Number", password.contains(where: { $0.isNumber })),
            ("Symbol", password.contains(where: { !$0.isLetter && !$0.isNumber })),
        ]
    }

    private var score: Int { checks.filter(\.1).count }

    private var quality: String {
        switch score {
        case 0...1: return "Weak"
        case 2...3: return "Fair"
        case 4: return "Good"
        default: return "Strong"
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Password quality: \(quality)")
                .font(.subheadline.weight(.semibold))
            ProgressView(value: Double(score), total: Double(checks.count))
            ForEach(Array(checks.enumerated()), id: \.offset) { _, check in
                Label(check.0, systemImage: check.1 ? "checkmark.circle.fill" : "circle")
                    .font(.caption)
                    .foregroundStyle(check.1 ? Color(red: 51 / 255, green: 51 / 255, blue: 51 / 255) : Color.secondary)
            }
            if !confirmPassword.isEmpty {
                Label(
                    "Passwords match",
                    systemImage: password == confirmPassword ? "checkmark.circle.fill" : "circle"
                )
                .font(.caption)
                .foregroundStyle(password == confirmPassword ? Color(red: 51 / 255, green: 51 / 255, blue: 51 / 255) : Color.secondary)
            }
            Text("Required: at least 8 characters. Other checks improve password strength.")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .accessibilityElement(children: .combine)
    }
}

struct AccessPasswordField: View {
    let title: String
    @Binding var text: String
    @Binding var isVisible: Bool
    var isError: Bool = false

    var body: some View {
        HStack(spacing: 8) {
            Group {
                if isVisible {
                    TextField(title, text: $text)
                } else {
                    SecureField(title, text: $text)
                }
            }
            .textInputAutocapitalization(.never)

            Button {
                isVisible.toggle()
            } label: {
                Image(systemName: isVisible ? "eye.slash" : "eye")
                    .accessibilityLabel(isVisible ? "Hide password" : "Show password")
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 12)
        .frame(minHeight: 44)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(isError ? Color.red : Color.secondary.opacity(0.45), lineWidth: 1)
        )
    }
}