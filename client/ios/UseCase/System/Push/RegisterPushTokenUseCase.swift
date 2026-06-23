import Foundation

public struct RegisterPushTokenUseCase {
    private let pushTokenRegistrar: PushTokenRegistrar

    public init(pushTokenRegistrar: PushTokenRegistrar) {
        self.pushTokenRegistrar = pushTokenRegistrar
    }

    public func callAsFunction(token: String, platform: String = "ios") async throws -> Bool {
        try await pushTokenRegistrar.register(
            payload: PushRegistrationPayload(token: token, platform: platform)
        )
    }
}
