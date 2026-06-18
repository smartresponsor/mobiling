import Foundation

public struct PushRegistrationPayload {
    public let token: String
    public let platform: String

    public init(token: String, platform: String = "ios") {
        self.token = token
        self.platform = platform
    }
}
