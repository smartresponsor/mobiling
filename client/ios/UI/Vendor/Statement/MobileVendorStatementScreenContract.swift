import Foundation

public struct MobileVendorStatementScreenContract: Sendable {
    public let vendorId: String
    public let statementStatus: String?
    public let currency: String?
    public let grossAmount: Double
    public let netAmount: Double

    public init(payload: MobileVendorStatementPayload) { vendorId = payload.vendorId; statementStatus = payload.statementStatus; currency = payload.currency; grossAmount = payload.grossAmount; netAmount = payload.netAmount }
}
