import Foundation

public struct Billing {
  private let uploadReceiptUseCase: UploadReceiptUseCase
  private let verifyReceiptUseCase: VerifyReceiptUseCase
  private let billingReceiptGateway: BillingReceiptGateway

  public init(
    billingReceiptGateway: BillingReceiptGateway = BillingReceiptGateway()
  ) {
    self.billingReceiptGateway = billingReceiptGateway
    self.uploadReceiptUseCase = UploadReceiptUseCase(billingReceiptGateway: billingReceiptGateway)
    self.verifyReceiptUseCase = VerifyReceiptUseCase(billingReceiptGateway: billingReceiptGateway)
  }

  public func uploadReceipt(token: String, product: String) async throws -> Bool {
    try await uploadReceiptUseCase(token: token, product: product)
  }

  public func verify(token: String) async throws -> Bool {
    let result = try await verifyReceiptUseCase(token: token)
    return result.accepted
  }

  public func uploadPayload(token: String, product: String) -> ReceiptUploadPayload {
    ReceiptUploadPayload(token: token, product: product)
  }

  public func defaultVerificationResult(accepted: Bool = false) -> ReceiptVerificationResult {
    ReceiptVerificationResult(accepted: accepted)
  }
}
