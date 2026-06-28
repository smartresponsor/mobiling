import Foundation

public struct HttpVendorStatementGateway: VendorStatementGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func loadVendorStatement(vendorId: String) async throws -> MobileVendorStatementPayload {
        guard let encodedVendorId = vendorId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: normalizedBaseUrl() + "/vendor/statement/" + encodedVendorId)
        else { throw HttpVendorStatementGatewayError.invalidUrl }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw HttpVendorStatementGatewayError.invalidResponse }
        guard (200..<300).contains(httpResponse.statusCode) else { throw HttpVendorStatementGatewayError.requestFailed("Mobile vendor statement request failed.") }
        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw HttpVendorStatementGatewayError.invalidPayload }

        return MobileVendorStatementPayload(
            vendorId: string(object["vendorId"]) ?? vendorId,
            statementStatus: string(object["statementStatus"]),
            currency: string(object["currency"]),
            grossAmount: double(object["grossAmount"]) ?? 0,
            netAmount: double(object["netAmount"]) ?? 0
        )
    }

    private func string(_ value: Any?) -> String? {
        if let value = value as? String { let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines); return trimmed.isEmpty ? nil : trimmed }
        if let value = value as? NSNumber { return value.stringValue }
        return nil
    }

    private func double(_ value: Any?) -> Double? { if let value = value as? Double { return value }; if let value = value as? NSNumber { return value.doubleValue }; if let value = value as? String { return Double(value.trimmingCharacters(in: .whitespacesAndNewlines)) }; return nil }

    private func normalizedBaseUrl() -> String { String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed()) }
}

public enum HttpVendorStatementGatewayError: Error { case invalidUrl; case invalidResponse; case invalidPayload; case requestFailed(String) }
