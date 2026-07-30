import SwiftUI

public struct MobilingAppShell: View {
    @State private var currentScreen: AccessScreen = .welcome
    @State private var activeVendorId: String?
    private let authFeatureBridge: AuthFeatureBridge?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?
    private let navigationShellGateway: NavigationShellGateway?
    private let vendorProfileGateway: VendorProfileGateway?
    private let vendorSummaryGateway: VendorSummaryGateway?
    private let vendorStatementGateway: VendorStatementGateway?
    private let vendorPayoutGateway: VendorPayoutGateway?
    private let vendorTransactionGateway: VendorTransactionGateway?
    private let vendorCrudGateway: VendorCrudGateway?
    private let initialRoute: String
    private let catalogEnabled: Bool

    public init(authFeatureBridge: AuthFeatureBridge? = nil, attachmentFeatureBridge: AttachmentFeatureBridge? = nil, navigationShellGateway: NavigationShellGateway? = nil, vendorProfileGateway: VendorProfileGateway? = nil, vendorSummaryGateway: VendorSummaryGateway? = nil, vendorStatementGateway: VendorStatementGateway? = nil, vendorPayoutGateway: VendorPayoutGateway? = nil, vendorTransactionGateway: VendorTransactionGateway? = nil, vendorCrudGateway: VendorCrudGateway? = nil, initialRoute: String = "dashboard", catalogEnabled: Bool = true) {
        self.authFeatureBridge = authFeatureBridge
        self.attachmentFeatureBridge = attachmentFeatureBridge
        self.navigationShellGateway = navigationShellGateway
        self.vendorProfileGateway = vendorProfileGateway
        self.vendorSummaryGateway = vendorSummaryGateway
        self.vendorStatementGateway = vendorStatementGateway
        self.vendorPayoutGateway = vendorPayoutGateway
        self.vendorTransactionGateway = vendorTransactionGateway
        self.vendorCrudGateway = vendorCrudGateway
        self.initialRoute = initialRoute
        self.catalogEnabled = catalogEnabled
    }

    public var body: some View {
        Group {
            switch currentScreen {
            case .dashboard:
                MobileDashboardShellView(
                    navigationShellGateway: navigationShellGateway,
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
            currentScreen = .welcome
        }
    }
}

