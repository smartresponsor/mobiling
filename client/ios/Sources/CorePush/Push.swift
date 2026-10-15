import MobileClient
import Foundation

public struct Push {
  private let registerPushTokenUseCase: RegisterPushTokenUseCase
  private let pushTokenRegistrar: PushTokenRegistrar

  public init(baseUrl: String) {
    let registrar = PushTokenRegistrar(baseUrl: baseUrl)
    self.pushTokenRegistrar = registrar
    self.registerPushTokenUseCase = RegisterPushTokenUseCase(pushTokenRegistrar: registrar)
  }

  public func register(
    token: String,
    platform: String = "ios",
    appKey: String,
    deviceId: String,
    enabled: Bool = true
  ) async throws -> Bool {
    try await registerPushTokenUseCase(token: token, platform: platform, appKey: appKey, deviceId: deviceId, enabled: enabled)
  }

  public func registrationPayload(
    token: String,
    platform: String = "ios",
    appKey: String,
    deviceId: String,
    enabled: Bool = true
  ) -> PushRegistrationPayload {
    PushRegistrationPayload(token: token, platform: platform, appKey: appKey, deviceId: deviceId, enabled: enabled)
  }
}
