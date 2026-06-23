import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for auth entry flow.
public struct AuthFeatureBridge: Sendable {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func start(request: StartAuthRequest) async throws -> AuthSessionPayload {
        try await StartAuthUseCase(gateway: gateway)(request: request)
    }

    public func register(request: RegisterAuthRequest) async throws -> AuthSessionPayload {
        try await RegisterAuthUseCase(gateway: gateway)(request: request)
    }
}
