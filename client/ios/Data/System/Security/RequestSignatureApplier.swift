import CryptoKit
import Foundation

public struct RequestSignatureApplier {
    private let secret: String

    public init(secret: String) {
        self.secret = secret
    }

    public func headers(for path: String) -> SignatureHeaders {
        SignatureHeaders(signatureValue: sign(data: path))
    }

    public func apply(to request: URLRequest) -> URLRequest {
        var copy = request
        let path = request.url?.path ?? "/"
        let headers = headers(for: path)
        copy.setValue(headers.signatureValue, forHTTPHeaderField: headers.signatureHeaderName)
        return copy
    }

    private func sign(data: String) -> String {
        let key = SymmetricKey(data: Data(secret.utf8))
        let signature = HMAC<SHA256>.authenticationCode(for: Data(data.utf8), using: key)
        return Data(signature).map { String(format: "%02x", $0) }.joined()
    }
}
