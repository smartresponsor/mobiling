import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Order-owned shipment bridge.
//
// Active surface is gateway-first so the order-owned wrapper no longer depends on
// the flat ShipmentFeatureBridge for runtime behavior. A compatibility initializer
// remains for transitional call sites.
public struct OrderShipmentBridge: Sendable {
    private let gateway: any ShipmentTrackingGateway

    public init(gateway: any ShipmentTrackingGateway) {
        self.gateway = gateway
    }

    public init(feature: ShipmentFeatureBridge) {
        self.gateway = feature.gateway()
    }

    public func list(query: ListShipmentsQuery) async throws -> [ShipmentTrackingSummary] {
        try await ListShipmentsUseCase(gateway: gateway)(query: query)
    }

    public func detail(shipmentId: String) async throws -> ShipmentDetailPayload {
        try await LoadShipmentDetailUseCase(gateway: gateway)(shipmentId: shipmentId)
    }
}
