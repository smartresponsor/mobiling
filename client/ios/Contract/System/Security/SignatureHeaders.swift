import Foundation

public struct SignatureHeaders: Sendable {
    public let signatureHeaderName: String
    public let signatureValue: String

    public init(
        signatureHeaderName: String = "X-SR-Signature",
        signatureValue: String
    ) {
        self.signatureHeaderName = signatureHeaderName
        self.signatureValue = signatureValue
    }
}
