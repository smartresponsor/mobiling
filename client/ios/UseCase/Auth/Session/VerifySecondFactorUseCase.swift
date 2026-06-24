public struct VerifySecondFactorUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: VerifySecondFactorRequest) async throws -> AuthSessionPayload {
        try await gateway.verifySecondFactor(request: request)
    }
}
