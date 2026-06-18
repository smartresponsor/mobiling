import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Order-centric business bridge.
public struct OrderBusinessBridge: Sendable {
    public let order: OrderFeatureBridge
    public let shipment: OrderShipmentBridge
    public let taxation: OrderTaxationBridge

    public init(
        order: OrderFeatureBridge,
        shipment: OrderShipmentBridge,
        taxation: OrderTaxationBridge
    ) {
        self.order = order
        self.shipment = shipment
        self.taxation = taxation
    }

    public func ownedRouteMap() -> OrderOwnedRouteMap {
        OrderOwnedRouteMap()
    }
}
