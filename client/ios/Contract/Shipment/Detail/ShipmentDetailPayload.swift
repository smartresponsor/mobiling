import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ShipmentDetailPayload {
    let shipmentId: String
    let shipmentNumber: String
    let orderId: String
    let carrierCode: String
    let trackingNumber: String
    let currentStatusCode: String
    let latestEventLabel: String
    let destinationLabel: String
}
