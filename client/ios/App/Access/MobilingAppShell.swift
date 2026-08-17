import SwiftUI

public struct MobilingAppShell: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var currentScreen: AccessScreen
    @State private var activeVendorId: String?
    @State private var launchSplashMounted: Bool = true
    @State private var launchSplashVisible: Bool = true
    @State private var launchSplashDismissScheduled: Bool = false
    private let authFeatureBridge: AuthFeatureBridge?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?
    private let navigationShellGateway: NavigationShellGateway?
    private let messageFeatureBridge: MessageFeatureBridge?
    private let vendorProfileGateway: VendorProfileGateway?
    private let vendorSummaryGateway: VendorSummaryGateway?
    private let vendorStatementGateway: VendorStatementGateway?
    private let vendorPayoutGateway: VendorPayoutGateway?
    private let vendorTransactionGateway: VendorTransactionGateway?
    private let vendorCrudGateway: VendorCrudGateway?
    private let initialRoute: String
    private let publicInitialRoute: String
    private let catalogEnabled: Bool
    private let availableRetailKinds: [RetailKind]
    private let navigationLabelResolver: (String?, String, String) -> String

    public init(authFeatureBridge: AuthFeatureBridge? = nil, attachmentFeatureBridge: AttachmentFeatureBridge? = nil, navigationShellGateway: NavigationShellGateway? = nil, messageFeatureBridge: MessageFeatureBridge? = nil, vendorProfileGateway: VendorProfileGateway? = nil, vendorSummaryGateway: VendorSummaryGateway? = nil, vendorStatementGateway: VendorStatementGateway? = nil, vendorPayoutGateway: VendorPayoutGateway? = nil, vendorTransactionGateway: VendorTransactionGateway? = nil, vendorCrudGateway: VendorCrudGateway? = nil, initialRoute: String = "dashboard", publicInitialRoute: String = "home", catalogEnabled: Bool = true, availableRetailKinds: [RetailKind] = RetailKind.allCases, navigationLabelResolver: @escaping (String?, String, String) -> String = { _, _, label in label }) {
        self.authFeatureBridge = authFeatureBridge
        self.attachmentFeatureBridge = attachmentFeatureBridge
        self.navigationShellGateway = navigationShellGateway
        self.messageFeatureBridge = messageFeatureBridge
        self.vendorProfileGateway = vendorProfileGateway
        self.vendorSummaryGateway = vendorSummaryGateway
        self.vendorStatementGateway = vendorStatementGateway
        self.vendorPayoutGateway = vendorPayoutGateway
        self.vendorTransactionGateway = vendorTransactionGateway
        self.vendorCrudGateway = vendorCrudGateway
        self.initialRoute = initialRoute
        self.publicInitialRoute = publicInitialRoute
        _currentScreen = State(initialValue: publicInitialRoute == "sign-in" ? .signIn : .welcome)
        self.catalogEnabled = catalogEnabled
        self.availableRetailKinds = availableRetailKinds
        self.navigationLabelResolver = navigationLabelResolver
    }

    public var body: some View {
        ZStack {
            Group {
                switch currentScreen {
            case .dashboard:
                MobileDashboardShellView(
                    navigationShellGateway: navigationShellGateway,
                    messageFeatureBridge: messageFeatureBridge,
                    attachmentFeatureBridge: attachmentFeatureBridge,
                    vendorId: activeVendorId,
                    vendorProfileGateway: vendorProfileGateway,
                    vendorSummaryGateway: vendorSummaryGateway,
                    vendorStatementGateway: vendorStatementGateway,
                    vendorPayoutGateway: vendorPayoutGateway,
                    vendorTransactionGateway: vendorTransactionGateway,
                    vendorCrudGateway: vendorCrudGateway,
                    initialRoute: initialRoute,
                    catalogEnabled: catalogEnabled,
                    availableRetailKinds: availableRetailKinds,
                    navigationLabelResolver: navigationLabelResolver,
                    onSignOut: { clearAccessSession() }
                )
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
                    onAccessSession: { payload in applyAccessSession(payload) }
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
                    onAccessSession: { payload in applyAccessSession(payload) }
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
                    onAccessSession: { payload in applyAccessSession(payload) }
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
                    onAccessSession: { payload in applyAccessSession(payload) }
                )
                }
            }

            if launchSplashMounted {
                OneTaskerLaunchSplashView(isVisible: launchSplashVisible)
                    .zIndex(10)
            }
        }
        .onAppear { scheduleLaunchSplashDismiss() }
        .task {
            guard let authFeatureBridge else {
                return
            }

            do {
                applyAccessSession(try await authFeatureBridge.restore())
            } catch {
            }
        }
    }

    private func applyAccessSession(_ payload: AuthSessionPayload) {
        activeVendorId = payload.vendorId
        currentScreen = payload.toAccessScreen()
    }

    private func clearAccessSession() {
        Task {
            do {
                try await authFeatureBridge?.logout()
            } catch {
            }

            activeVendorId = nil
            currentScreen = publicInitialRoute == "sign-in" ? .signIn : .welcome
        }
    }

    private func scheduleLaunchSplashDismiss() {
        guard !launchSplashDismissScheduled else {
            return
        }

        launchSplashDismissScheduled = true
        Task { @MainActor in
            let holdNanoseconds: UInt64 = reduceMotion ? 360_000_000 : 1_760_000_000
            let fadeNanoseconds: UInt64 = reduceMotion ? 120_000_000 : 320_000_000

            try? await Task.sleep(nanoseconds: holdNanoseconds)
            withAnimation(.easeOut(duration: reduceMotion ? 0.08 : 0.28)) {
                launchSplashVisible = false
            }

            try? await Task.sleep(nanoseconds: fadeNanoseconds)
            launchSplashMounted = false
        }
    }
}

