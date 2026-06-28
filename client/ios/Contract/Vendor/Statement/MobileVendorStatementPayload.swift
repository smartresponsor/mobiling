import Foundation

public struct MobileVendorStatementPayload: Sendable {
    public let vendorId: String
    public let statementStatus: String?
    public let currency: String?
    public let grossAmount: Double
    public let netAmount: Double
}
