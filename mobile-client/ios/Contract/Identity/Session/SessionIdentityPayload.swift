public struct SessionIdentityPayload {
    public let vendorId: String?
    public let accountId: String?
    public let displayName: String?
    public let activeRole: String?
    public let authenticated: Bool
    public let emailVerified: Bool
    public let secondFactorEnabled: Bool

    public init(vendorId: String?, accountId: String?, displayName: String?, activeRole: String?, authenticated: Bool, emailVerified: Bool, secondFactorEnabled: Bool) {
        self.vendorId = vendorId
        self.accountId = accountId
        self.displayName = displayName
        self.activeRole = activeRole
        self.authenticated = authenticated
        self.emailVerified = emailVerified
        self.secondFactorEnabled = secondFactorEnabled
    }
}
