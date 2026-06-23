import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol ShipmentTrackingGateway {
    func listShipments(query: ListShipmentsQuery) async throws -> [ShipmentTrackingSummary]

    func loadShipmentDetail(shipmentId: String) async throws -> ShipmentDetailPayload
}
