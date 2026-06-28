import Foundation

public struct HttpVendorSummaryGateway: VendorSummaryGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func loadVendorSummary(vendorId: String) async throws -> MobileVendorSummaryPayload {
        guard let encodedVendorId = vendorId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: normalizedBaseUrl() + "/vendor/summary/" + encodedVendorId)
        else { throw HttpVendorSummaryGatewayError.invalidUrl }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else { throw HttpVendorSummaryGatewayError.invalidResponse }
        guard (200..<300).contains(httpResponse.statusCode) else { throw HttpVendorSummaryGatewayError.requestFailed("Mobile vendor summary request failed.") }

        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw HttpVendorSummaryGatewayError.invalidPayload }

        return MobileVendorSummaryPayload(
            vendorId: string(object["vendorId"]) ?? vendorId,
            brandName: string(object["brandName"]),
            status: string(object["status"]),
            profileCompletionPercent: min(100, max(0, int(object["profileCompletionPercent"]) ?? 0)),
            nextAction: string(object["nextAction"])
        )
    }

    private func string(_ value: Any?) -> String? {
        if let value = value as? String { let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines); return trimmed.isEmpty ? nil : trimmed }
        if let value = value as? NSNumber { return value.stringValue }
        return nil
    }

    private func int(_ value: Any?) -> Int? {
        if let value = value as? Int { return value }
        if let value = value as? NSNumber { return value.intValue }
        if let value = value as? String { return Int(value.trimmingCharacters(in: .whitespacesAndNewlines)) }
        return nil
    }

    private func normalizedBaseUrl() -> String { String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed()) }
}

public enum HttpVendorSummaryGatewayError: Error { case invalidUrl; case invalidResponse; case invalidPayload; case requestFailed(String) }
