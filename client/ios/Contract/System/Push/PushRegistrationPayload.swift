import Foundation

public struct PushRegistrationPayload: Codable {
    public let token: String
    public let platform: String
    public let appKey: String
    public let deviceId: String
    public let enabled: Bool

    public init(token: String, platform: String = "ios", appKey: String, deviceId: String, enabled: Bool = true) {
        self.token = token
        self.platform = platform
        self.appKey = appKey
        self.deviceId = deviceId
        self.enabled = enabled
    }
}
