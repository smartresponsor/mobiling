import Foundation

public struct UploadReceiptUseCase {
    private let billingReceiptGateway: BillingReceiptGateway

    public init(billingReceiptGateway: BillingReceiptGateway) {
        self.billingReceiptGateway = billingReceiptGateway
    }

    public func callAsFunction(token: String, product: String) async throws -> Bool {
        try await billingReceiptGateway.upload(
            payload: ReceiptUploadPayload(token: token, product: product)
        )
    }
}
