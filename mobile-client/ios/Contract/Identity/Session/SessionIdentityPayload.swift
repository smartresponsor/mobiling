public struct SessionIdentityPayload {
    public let userId: String?
    public let accountId: String?
    public let displayName: String?
    public let activeRole: String?
    public let authenticated: Bool
    public let emailVerified: Bool
    public let secondFactorEnabled: Bool

    public init(userId: String?, accountId: String?, displayName: String?, activeRole: String?, authenticated: Bool, emailVerified: Bool, secondFactorEnabled: Bool) {
        self.userId = userId
        self.accountId = accountId
        self.displayName = displayName
        self.activeRole = activeRole
        self.authenticated = authenticated
        self.emailVerified = emailVerified
        self.secondFactorEnabled = secondFactorEnabled
    }
}
