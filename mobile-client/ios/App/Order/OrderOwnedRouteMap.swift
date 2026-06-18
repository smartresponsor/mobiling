import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Canonical route ownership map for order-owned flows.
public struct OrderOwnedRouteMap: Sendable {
    public init() {}

    public func ownedFlows() -> [OrderOwnedFlow] {
        [.shipment, .taxation]
    }
}
