import Foundation

public struct ApplyRequestSignatureUseCase {
    private let requestSignatureApplier: RequestSignatureApplier

    public init(requestSignatureApplier: RequestSignatureApplier) {
        self.requestSignatureApplier = requestSignatureApplier
    }

    public func callAsFunction(request: URLRequest) -> URLRequest {
        requestSignatureApplier.apply(to: request)
    }
}
