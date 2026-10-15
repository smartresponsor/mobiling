import Foundation

public struct RetailPlacementSnapshot: Sendable {
    public let retailId: String
    public let kind: String
    public let catalogCode: String?
    public let categoryId: String?
    public let title: String?
    public let status: String?
    public let nextStep: String
    public let requiresExactLocation: Bool
    public let fulfillmentProfile: String?
    public let locationProfile: String?
    public let pricingProfile: String?
}

public protocol RetailPlacementGateway: Sendable {
    func snapshot(retailId: String) async throws -> RetailPlacementSnapshot
    func configureFulfillment(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot
    func configureLocation(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot
    func configurePricing(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot
    func publish(retailId: String) async throws -> RetailPlacementSnapshot
}

public final class HttpRetailPlacementGateway: RetailPlacementGateway, @unchecked Sendable {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func snapshot(retailId: String) async throws -> RetailPlacementSnapshot {
        try await request(method: "GET", retailId: retailId, step: "placement", fields: nil)
    }

    public func configureFulfillment(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot {
        try await request(method: "POST", retailId: retailId, step: "fulfillment", fields: fields)
    }

    public func configureLocation(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot {
        try await request(method: "POST", retailId: retailId, step: "location", fields: fields)
    }

    public func configurePricing(retailId: String, fields: [String: String]) async throws -> RetailPlacementSnapshot {
        try await request(method: "POST", retailId: retailId, step: "pricing", fields: fields)
    }

    public func publish(retailId: String) async throws -> RetailPlacementSnapshot {
        try await request(method: "POST", retailId: retailId, step: "publish", fields: [:])
    }

    private func request(method: String, retailId: String, step: String, fields: [String: String]?) async throws -> RetailPlacementSnapshot {
        guard retailId.range(of: "^[1-9][0-9]*$", options: .regularExpression) != nil else {
            throw NSError(domain: "RetailPlacement", code: 400, userInfo: [NSLocalizedDescriptionKey: "Retail identity must be a positive integer."])
        }
        guard let url = URL(string: baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/retail/\(retailId)/\(step)") else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let fields {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: fields)
        }
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw URLError(.badServerResponse) }
        let payload = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any] ?? [:]
        guard (200..<300).contains(http.statusCode) else {
            throw NSError(domain: "RetailPlacement", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: payload["message"] as? String ?? "Retail placement request failed."])
        }
        return Self.snapshot(payload)
    }

    private static func snapshot(_ payload: [String: Any]) -> RetailPlacementSnapshot {
        RetailPlacementSnapshot(
            retailId: String(describing: payload["retailId"] ?? ""),
            kind: payload["kind"] as? String ?? "",
            catalogCode: payload["catalogCode"] as? String,
            categoryId: payload["categoryId"].map { String(describing: $0) },
            title: payload["title"] as? String,
            status: payload["status"] as? String,
            nextStep: payload["nextStep"] as? String ?? "fulfillment",
            requiresExactLocation: payload["requiresExactLocation"] as? Bool ?? false,
            fulfillmentProfile: jsonString(payload["fulfillmentProfile"]),
            locationProfile: jsonString(payload["locationProfile"]),
            pricingProfile: jsonString(payload["pricingProfile"])
        )
    }

    private static func jsonString(_ value: Any?) -> String? {
        guard let value, JSONSerialization.isValidJSONObject(value),
              let data = try? JSONSerialization.data(withJSONObject: value) else { return nil }
        return String(data: data, encoding: .utf8)
    }
}
