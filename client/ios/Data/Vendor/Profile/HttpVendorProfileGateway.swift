import Foundation

public struct HttpVendorProfileGateway: VendorProfileGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func loadVendorProfile(vendorId: String) async throws -> MobileVendorProfilePayload {
        guard let encodedVendorId = vendorId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: normalizedBaseUrl() + "/vendor/profile/" + encodedVendorId)
        else {
            throw HttpVendorProfileGatewayError.invalidUrl
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpVendorProfileGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpVendorProfileGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        return try payload(from: data, fallbackVendorId: vendorId)
    }

    private func payload(from data: Data, fallbackVendorId: String) throws -> MobileVendorProfilePayload {
        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw HttpVendorProfileGatewayError.invalidPayload
        }

        return MobileVendorProfilePayload(
            vendorId: string(object["vendorId"]) ?? fallbackVendorId,
            displayName: string(object["displayName"]),
            brandName: string(object["brandName"]),
            status: string(object["status"]),
            completionPercent: min(100, max(0, int(object["completionPercent"]) ?? 0)),
            readyForPublishing: object["readyForPublishing"] as? Bool ?? false,
            nextAction: string(object["nextAction"]),
            avatarUrl: string(object["avatarUrl"]),
            coverUrl: string(object["coverUrl"]),
            about: string(object["about"]),
            website: string(object["website"]),
            publicationStatus: string(object["publicationStatus"])
        )
    }

    private func string(_ value: Any?) -> String? {
        if let value = value as? String {
            let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }

        if let value = value as? NSNumber {
            return value.stringValue
        }

        return nil
    }

    private func int(_ value: Any?) -> Int? {
        if let value = value as? Int {
            return value
        }

        if let value = value as? NSNumber {
            return value.intValue
        }

        if let value = value as? String {
            return Int(value.trimmingCharacters(in: .whitespacesAndNewlines))
        }

        return nil
    }

    private func errorMessage(data: Data, statusCode: Int) -> String {
        guard !data.isEmpty,
              let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return "Mobile vendor profile request failed with HTTP \(statusCode)."
        }

        let code = object["code"] as? String ?? "mobile_vendor_profile_error"
        let message = object["message"] as? String ?? "Mobile vendor profile request failed."

        return "\(code): \(message)"
    }

    private func normalizedBaseUrl() -> String {
        String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed())
    }
}

public enum HttpVendorProfileGatewayError: Error {
    case invalidUrl
    case invalidResponse
    case invalidPayload
    case requestFailed(String)
}
