public struct RegisterAuthRequest {
    public let displayName: String
    public let email: String
    public let password: String
    public let deviceLabel: String?

    public init(displayName: String, email: String, password: String, deviceLabel: String?) {
        self.displayName = displayName
        self.email = email
        self.password = password
        self.deviceLabel = deviceLabel
    }
}