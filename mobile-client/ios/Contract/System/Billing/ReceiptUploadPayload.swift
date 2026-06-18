import Foundation

public struct ReceiptUploadPayload {
    public let token: String
    public let product: String

    public init(token: String, product: String) {
        self.token = token
        self.product = product
    }
}
