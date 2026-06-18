import Foundation

public struct RemoteConfigStore {
    public init() {}

    public func refresh(baseUrl: String = "https://httpbin.org") async throws -> Bool {
        let url = URL(string: baseUrl + "/anything/mobile/config")!
        _ = try await URLSession.shared.data(from: url)
        UserDefaults.standard.set(defaultPayloadJson(), forKey: "rc.payload")
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: "rc.fetchedAt")
        return true
    }

    public func isFresh(ttlSec: Int = 1800) -> Bool {
        let fetchedAt = UserDefaults.standard.double(forKey: "rc.fetchedAt")
        return Date().timeIntervalSince1970 - fetchedAt < Double(ttlSec)
    }

    public func defaultPayload() -> RemoteConfigPayload {
        RemoteConfigPayload(
            ttlSec: 1800,
            analyticEnabled: true,
            billingGraceDay: 3,
            securitySigningEnabled: true
        )
    }

    private func defaultPayloadJson() -> String {
        #"{"ttlSec":1800,"flag":{"analyticEnabled":true,"billing.graceDay":3,"security.signingEnabled":true}}"#
    }
}
