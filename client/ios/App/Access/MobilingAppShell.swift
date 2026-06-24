import SwiftUI

public struct MobilingAppShell: View {
    @State private var currentScreen: AccessScreen = .welcome
    private let authFeatureBridge: AuthFeatureBridge?

    public init(authFeatureBridge: AuthFeatureBridge? = nil) {
        self.authFeatureBridge = authFeatureBridge
    }

    public var body: some View {
        Group {
            switch currentScreen {
            case .welcome:
                AccessWelcomeView(
                    onSignIn: { currentScreen = .signIn },
                    onCreateAccess: { currentScreen = .register }
                )
            case .signIn:
                SignInView(
                    onBack: { currentScreen = .welcome },
                    onCreateAccess: { currentScreen = .register },
                    onRecoverAccess: { currentScreen = .recoveryRequest },
                    onStartAccess: { request in
                        guard let authFeatureBridge else {
                            return nil
                        }
                        return try await authFeatureBridge.start(request: request)
                    },
                    onAccessSession: { payload in currentScreen = payload.toAccessScreen() }
                )
            case .register:
                RegisterAccessView(
                    onBack: { currentScreen = .welcome },
                    onSignIn: { currentScreen = .signIn },
                    onRegisterAccess: { request in
                        guard let authFeatureBridge else {
                            return nil
                        }
                        return try await authFeatureBridge.register(request: request)
                    },
                    onAccessSession: { payload in currentScreen = payload.toAccessScreen() }
                )
            case .verificationRequired:
                VerificationRequiredView(
                    onBack: { currentScreen = .signIn },
                    onUseDifferentAccess: { clearAccessSession() }
                )
            case .secondFactorRequired:
                SecondFactorRequiredView(
                    onBack: { currentScreen = .signIn },
                    onUseDifferentAccess: { clearAccessSession() }
                )
            case .recoveryRequest:
                RecoveryRequestView(
                    onBack: { currentScreen = .signIn },
                    onHaveRecoveryCode: { currentScreen = .recoveryReset },
                    onRequestRecovery: { request in
                        guard let authFeatureBridge else {
                            return nil
                        }
                        return try await authFeatureBridge.requestRecovery(request: request)
                    },
                    onAccessSession: { payload in currentScreen = payload.toAccessScreen() }
                )
            case .recoveryReset:
                RecoveryResetView(
                    onBack: { currentScreen = .recoveryRequest },
                    onRequestRecovery: { currentScreen = .recoveryRequest },
                    onResetRecovery: { request in
                        guard let authFeatureBridge else {
                            return nil
                        }
                        return try await authFeatureBridge.resetRecovery(request: request)
                    },
                    onAccessSession: { payload in currentScreen = payload.toAccessScreen() }
                )
            }
        }
        .task {
            guard let authFeatureBridge else {
                return
            }

            do {
                currentScreen = try await authFeatureBridge.restore().toAccessScreen()
            } catch {
            }
        }
    }

    private func clearAccessSession() {
        Task {
            do {
                try await authFeatureBridge?.logout()
            } catch {
            }

            currentScreen = .welcome
        }
    }
}