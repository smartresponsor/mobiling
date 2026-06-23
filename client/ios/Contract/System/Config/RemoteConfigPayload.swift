import Foundation

public struct RemoteConfigPayload: Sendable {
    public let ttlSec: Int
    public let analyticEnabled: Bool
    public let billingGraceDay: Int
    public let securitySigningEnabled: Bool

    public init(
        ttlSec: Int,
        analyticEnabled: Bool,
        billingGraceDay: Int,
        securitySigningEnabled: Bool
    ) {
        self.ttlSec = ttlSec
        self.analyticEnabled = analyticEnabled
        self.billingGraceDay = billingGraceDay
        self.securitySigningEnabled = securitySigningEnabled
    }
}
