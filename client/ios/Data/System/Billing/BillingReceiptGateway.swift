import Foundation

public struct BillingReceiptGateway {
    private let baseUrl: String

    public init(baseUrl: String = "https://httpbin.org") {
        self.baseUrl = baseUrl
    }

    public func upload(payload: ReceiptUploadPayload) async throws -> Bool {
        let url = URL(string: baseUrl + "/anything/mobile/receipt")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = try JSONSerialization.data(
            withJSONObject: ["token": payload.token, "product": payload.product]
        )
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    public func verify(token: String) async throws -> ReceiptVerificationResult {
        let url = URL(string: baseUrl + "/anything/mobile/verify")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        let (_, response) = try await URLSession.shared.data(for: request)
        return ReceiptVerificationResult(
            accepted: (response as? HTTPURLResponse)?.statusCode == 200
        )
    }
}
