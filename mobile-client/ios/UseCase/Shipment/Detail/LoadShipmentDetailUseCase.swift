import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadShipmentDetailUseCase {
    let shipmentTrackingGateway: ShipmentTrackingGateway

    func callAsFunction(shipmentId: String) async throws -> ShipmentDetailPayload {
        try await shipmentTrackingGateway.loadShipmentDetail(shipmentId: shipmentId)
    }
}
