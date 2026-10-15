import Foundation

public protocol CurrencyGateway {
    func loadCatalog(locale: String?) async throws -> CurrencyCatalogPayload
    func loadSelector(selectedCode: String?, locale: String?) async throws -> CurrencySelectorPayload
}

public struct CurrencyHttpGateway: CurrencyGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func loadCatalog(locale: String? = nil) async throws -> CurrencyCatalogPayload {
        try await request(path: "/currency/catalog", query: ["locale": locale])
    }

    public func loadSelector(selectedCode: String? = nil, locale: String? = nil) async throws -> CurrencySelectorPayload {
        try await request(path: "/currency/selector", query: ["selectedCode": selectedCode, "locale": locale])
    }

    private func request<Response: Decodable>(path: String, query: [String: String?]) async throws -> Response {
        guard var components = URLComponents(string: baseUrl + path) else { throw CurrencyGatewayError.invalidBaseUrl }
        components.queryItems = query.compactMap { key, value in
            guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
            return URLQueryItem(name: key, value: value)
        }
        guard let url = components.url else { throw CurrencyGatewayError.invalidBaseUrl }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw CurrencyGatewayError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else { throw CurrencyGatewayError.requestFailed(http.statusCode) }
        do {
            return try JSONDecoder().decode(Response.self, from: data)
        } catch {
            throw CurrencyGatewayError.invalidPayload(error.localizedDescription)
        }
    }
}

private enum CurrencyGatewayError: LocalizedError {
    case invalidBaseUrl
    case invalidResponse
    case invalidPayload(String)
    case requestFailed(Int)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl: return "Currency gateway URL is invalid."
        case .invalidResponse: return "Currency gateway returned an invalid response."
        case .invalidPayload(let message): return "Currency response could not be decoded: \(message)"
        case .requestFailed(let status): return "Currency request failed with HTTP \(status)."
        }
    }
}

