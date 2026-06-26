public struct AuthSessionPayload {
    public let status: String
    public let sessionId: String?
    public let vendorId: String?
    public let authenticated: Bool
    public let requiresVerification: Bool
    public let requiresSecondFactor: Bool

    public init(status: String, sessionId: String?, vendorId: String?, authenticated: Bool, requiresVerification: Bool, requiresSecondFactor: Bool) {
        self.status = status
        self.sessionId = sessionId
        self.vendorId = vendorId
        self.authenticated = authenticated
        self.requiresVerification = requiresVerification
        self.requiresSecondFactor = requiresSecondFactor
    }
}
