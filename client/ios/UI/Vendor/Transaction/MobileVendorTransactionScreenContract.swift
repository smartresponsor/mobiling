import Foundation

public struct MobileVendorTransactionScreenContract: Sendable {
    public let vendorId: String
    public let transactions: [MobileVendorTransactionItemPayload]

    public init(payload: MobileVendorTransactionPayload) {
        vendorId = payload.vendorId
        transactions = payload.transactions
    }
}
