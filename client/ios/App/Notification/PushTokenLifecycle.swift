import Foundation

public final class PushTokenLifecycle: @unchecked Sendable {
    public static let tokenDidChangeNotification = Notification.Name("MobilingPushTokenDidChange")

    private let defaults: UserDefaults

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    public var installationId: String {
        if let existing = defaults.string(forKey: Keys.installationId), !existing.isEmpty {
            return existing
        }
        let value = UUID().uuidString.lowercased()
        defaults.set(value, forKey: Keys.installationId)
        return value
    }

    public func recordToken(_ token: String) {
        let normalized = token.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalized.isEmpty else { return }
        defaults.set(normalized, forKey: Keys.token)
        NotificationCenter.default.post(name: Self.tokenDidChangeNotification, object: nil)
    }

    public func sync(bridge: NotificationFeatureBridge?, appKey: String) async -> Bool {
        guard let token = defaults.string(forKey: Keys.token), !token.isEmpty, let bridge else { return false }
        do {
            let ok = try await bridge.subscription(
                request: NotificationSubscriptionRequest(
                    token: token,
                    platform: "ios",
                    appKey: appKey,
                    deviceId: installationId,
                    enabled: true
                )
            )
            if ok { defaults.set(token, forKey: Keys.registeredToken) }
            return ok
        } catch {
            return false
        }
    }

    public func disable(bridge: NotificationFeatureBridge?, appKey: String) async -> Bool {
        guard let bridge else { return false }
        let token = defaults.string(forKey: Keys.registeredToken) ?? defaults.string(forKey: Keys.token)
        guard let token, !token.isEmpty else { return false }
        do {
            let ok = try await bridge.subscription(
                request: NotificationSubscriptionRequest(
                    token: token,
                    platform: "ios",
                    appKey: appKey,
                    deviceId: installationId,
                    enabled: false
                )
            )
            if ok { defaults.removeObject(forKey: Keys.registeredToken) }
            return ok
        } catch {
            return false
        }
    }

    private enum Keys {
        static let installationId = "mobiling.push.installation_id"
        static let token = "mobiling.push.token"
        static let registeredToken = "mobiling.push.registered_token"
    }
}
