import Foundation

public struct LoadNavigationShellUseCase {
    private let gateway: NavigationShellGateway

    public init(gateway: NavigationShellGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws -> MobileNavigationShellPayload { try await gateway.loadMobileShell() }
}
