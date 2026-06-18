import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for shipment-domain controlled rewire.
public struct ShipmentFeatureBridge: Sendable {
    private let trackingGateway: any ShipmentTrackingGateway

    public init(trackingGateway: any ShipmentTrackingGateway) {
        self.trackingGateway = trackingGateway
    }

    public func list(query: ListShipmentsQuery) async throws -> [ShipmentTrackingSummary] {
        try await ListShipmentsUseCase(gateway: trackingGateway)(query: query)
    }

    public func detail(shipmentId: String) async throws -> ShipmentDetailPayload {
        try await LoadShipmentDetailUseCase(gateway: trackingGateway)(shipmentId: shipmentId)
    }

    func gateway() -> any ShipmentTrackingGateway {
        trackingGateway
    }
}
