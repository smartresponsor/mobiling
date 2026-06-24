public struct ResetRecoveryRequest {
    public let code: String
    public let password: String

    public init(code: String, password: String) {
        self.code = code
        self.password = password
    }
}
