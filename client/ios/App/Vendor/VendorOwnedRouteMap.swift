import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Canonical route ownership map for vendor-owned flows.
public struct VendorOwnedRouteMap: Sendable {
    public init() {}

    public func ownedFlows() -> [VendorOwnedFlow] {
        [.product, .order, .project, .profile]
    }
}
