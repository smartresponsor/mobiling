import SwiftUI

public struct MobilingAppShell: View {
    @State private var currentScreen: AccessScreen = .welcome

    public init() {}

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
                    onCreateAccess: { currentScreen = .register }
                )
            case .register:
                RegisterAccessView(
                    onBack: { currentScreen = .welcome },
                    onSignIn: { currentScreen = .signIn }
                )
            }
        }
    }
}
