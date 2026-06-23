public struct LoadProfileDetailUseCase {
    private let gateway: ProfileDetailGateway

    public init(gateway: ProfileDetailGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(profileId: String) async throws -> ProfileDetailPayload {
        try await gateway.loadProfileDetail(profileId: profileId)
    }
}
