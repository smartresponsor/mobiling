import Foundation

public struct RegisterPushTokenUseCase {
    private let pushTokenRegistrar: PushTokenRegistrar

    public init(pushTokenRegistrar: PushTokenRegistrar) {
        self.pushTokenRegistrar = pushTokenRegistrar
    }

    public func callAsFunction(
        token: String,
        platform: String = "ios",
        appKey: String,
        deviceId: String,
        enabled: Bool = true
    ) async throws -> Bool {
        try await pushTokenRegistrar.register(
            payload: PushRegistrationPayload(token: token, platform: platform, appKey: appKey, deviceId: deviceId, enabled: enabled)
        )
    }
}
