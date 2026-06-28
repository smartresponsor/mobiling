import Foundation

public struct HttpVendorPayoutGateway: VendorPayoutGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func loadVendorPayout(vendorId: String) async throws -> MobileVendorPayoutPayload {
        guard let encodedVendorId = vendorId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: normalizedBaseUrl() + "/vendor/payout/" + encodedVendorId)
        else { throw HttpVendorPayoutGatewayError.invalidUrl }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw HttpVendorPayoutGatewayError.invalidResponse }
        guard (200..<300).contains(httpResponse.statusCode) else { throw HttpVendorPayoutGatewayError.requestFailed("Mobile vendor payout request failed.") }
        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw HttpVendorPayoutGatewayError.invalidPayload }

        return MobileVendorPayoutPayload(
            vendorId: string(object["vendorId"]) ?? vendorId,
            payoutStatus: string(object["payoutStatus"]),
            currency: string(object["currency"]),
            availableAmount: double(object["availableAmount"]) ?? 0,
            pendingAmount: double(object["pendingAmount"]) ?? 0,
            payoutAccountLabel: string(object["payoutAccountLabel"])
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

public enum HttpVendorPayoutGatewayError: Error { case invalidUrl; case invalidResponse; case invalidPayload; case requestFailed(String) }
