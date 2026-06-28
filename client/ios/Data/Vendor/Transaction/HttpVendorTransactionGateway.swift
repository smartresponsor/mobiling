import Foundation

public struct HttpVendorTransactionGateway: VendorTransactionGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) { self.baseUrl = baseUrl; self.session = session }

    public func loadVendorTransaction(vendorId: String) async throws -> MobileVendorTransactionPayload {
        guard let encodedVendorId = vendorId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: normalizedBaseUrl() + "/vendor/transaction/" + encodedVendorId)
        else { throw HttpVendorTransactionGatewayError.invalidUrl }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw HttpVendorTransactionGatewayError.invalidResponse }
        guard (200..<300).contains(httpResponse.statusCode) else { throw HttpVendorTransactionGatewayError.requestFailed("Mobile vendor transaction request failed.") }
        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw HttpVendorTransactionGatewayError.invalidPayload }
        let transactions = (object["transactions"] as? [[String: Any]] ?? []).map { item in
            MobileVendorTransactionItemPayload(
                id: string(item["id"]),
                status: string(item["status"]),
                type: string(item["type"]),
                amount: double(item["amount"]) ?? 0,
                currency: string(item["currency"]),
                createdAt: string(item["createdAt"])
            )
        }
        return MobileVendorTransactionPayload(vendorId: string(object["vendorId"]) ?? vendorId, transactions: transactions)
    }

    private func string(_ value: Any?) -> String? {
        if let value = value as? String { let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines); return trimmed.isEmpty ? nil : trimmed }
        if let value = value as? NSNumber { return value.stringValue }
        return nil
    }

    private func double(_ value: Any?) -> Double? { if let value = value as? Double { return value }; if let value = value as? NSNumber { return value.doubleValue }; if let value = value as? String { return Double(value.trimmingCharacters(in: .whitespacesAndNewlines)) }; return nil }
    private func normalizedBaseUrl() -> String { String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed()) }
}

public enum HttpVendorTransactionGatewayError: Error { case invalidUrl; case invalidResponse; case invalidPayload; case requestFailed(String) }
