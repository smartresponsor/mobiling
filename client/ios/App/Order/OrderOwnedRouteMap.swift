import Foundation
public struct OrderOwnedRouteMap: Sendable {
    public init() {}
    public func ownedFlows() -> [OrderOwnedFlow] { [.shipment] }
    public func embeddedCapabilities() -> [OrderEmbeddedCapability] { [.taxation] }
}
