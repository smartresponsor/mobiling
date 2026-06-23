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
                    onUseDifferentAccess: { currentScreen = .welcome }
                )
            case .secondFactorRequired:
                SecondFactorRequiredView(
                    onBack: { currentScreen = .signIn },
                    onUseDifferentAccess: { currentScreen = .welcome }
                )
            }
        }
    }
}
