public struct StartAuthRequest {
    public let login: String
    public let password: String
    public let deviceLabel: String?

    public init(login: String, password: String, deviceLabel: String?) {
        self.login = login
        self.password = password
        self.deviceLabel = deviceLabel
    }
}
