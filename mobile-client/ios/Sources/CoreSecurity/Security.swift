import Foundation

public struct Security {
  private let applyRequestSignatureUseCase: ApplyRequestSignatureUseCase
  private let requestSignatureApplier: RequestSignatureApplier

  public init(secret: String = "secret") {
    let applier = RequestSignatureApplier(secret: secret)
    self.requestSignatureApplier = applier
    self.applyRequestSignatureUseCase = ApplyRequestSignatureUseCase(requestSignatureApplier: applier)
  }

  public func headers(for path: String) -> SignatureHeaders {
    requestSignatureApplier.headers(for: path)
  }

  public func applySignature(to req: URLRequest) -> URLRequest {
    applyRequestSignatureUseCase(request: req)
  }
}
