public struct ProfileDetailPayload {
    public let profileId: String
    public let displayName: String?
    public let email: String?
    public let phone: String?
    public let avatarUrl: String?

    public init(profileId: String, displayName: String?, email: String?, phone: String?, avatarUrl: String?) {
        self.profileId = profileId
        self.displayName = displayName
        self.email = email
        self.phone = phone
        self.avatarUrl = avatarUrl
    }
}
