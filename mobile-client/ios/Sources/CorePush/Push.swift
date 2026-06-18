import Foundation

public struct Push {
  private let registerPushTokenUseCase: RegisterPushTokenUseCase
  private let pushTokenRegistrar: PushTokenRegistrar

  public init(
    pushTokenRegistrar: PushTokenRegistrar = PushTokenRegistrar()
  ) {
    self.pushTokenRegistrar = pushTokenRegistrar
    self.registerPushTokenUseCase = RegisterPushTokenUseCase(pushTokenRegistrar: pushTokenRegistrar)
  }

  public func register(token: String, platform: String = "ios") async throws -> Bool {
    try await registerPushTokenUseCase(token: token, platform: platform)
  }

  public func registrationPayload(token: String, platform: String = "ios") -> PushRegistrationPayload {
    PushRegistrationPayload(token: token, platform: platform)
  }
}
