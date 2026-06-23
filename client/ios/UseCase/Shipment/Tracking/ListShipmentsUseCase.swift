import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ListShipmentsUseCase {
    let shipmentTrackingGateway: ShipmentTrackingGateway

    func callAsFunction(query: ListShipmentsQuery) async throws -> [ShipmentTrackingSummary] {
        try await shipmentTrackingGateway.listShipments(query: query)
    }
}
