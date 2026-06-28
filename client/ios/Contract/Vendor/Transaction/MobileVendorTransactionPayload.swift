import Foundation

public struct MobileVendorTransactionPayload: Sendable {
    public let vendorId: String
    public let transactions: [MobileVendorTransactionItemPayload]
}

public struct MobileVendorTransactionItemPayload: Sendable, Identifiable {
    public let id: String?
    public let status: String?
    public let type: String?
    public let amount: Double
    public let currency: String?
    public let createdAt: String?
}
