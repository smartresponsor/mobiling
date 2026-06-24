public struct ChallengeSecondFactorUseCase {
    private let gateway: AuthSessionGateway

    public init(gateway: AuthSessionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws -> AuthSessionPayload {
        try await gateway.challengeSecondFactor()
    }
}
