import Foundation

public struct VerifyReceiptUseCase {
    private let billingReceiptGateway: BillingReceiptGateway

    public init(billingReceiptGateway: BillingReceiptGateway) {
        self.billingReceiptGateway = billingReceiptGateway
    }

    public func callAsFunction(token: String) async throws -> ReceiptVerificationResult {
        try await billingReceiptGateway.verify(token: token)
    }
}
