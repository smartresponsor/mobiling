public protocol ProfileDetailGateway {
    func loadProfileDetail(profileId: String) async throws -> ProfileDetailPayload
}
