import Foundation

public protocol NavigationShellGateway {
    func loadMobileShell() async throws -> MobileNavigationShellPayload
}
