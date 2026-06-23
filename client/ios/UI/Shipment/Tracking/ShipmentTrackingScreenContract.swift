import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ShipmentTrackingScreenContract {
    let title: String
    let rows: [ShipmentTrackingSummary]
    let nextCursor: String?
}
