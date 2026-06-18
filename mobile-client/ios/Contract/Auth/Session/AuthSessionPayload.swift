public struct AuthSessionPayload {
    public let accessToken: String
    public let refreshToken: String?
    public let expiresAt: String
    public let sessionId: String

    public init(accessToken: String, refreshToken: String?, expiresAt: String, sessionId: String) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
        self.expiresAt = expiresAt
        self.sessionId = sessionId
    }
}
