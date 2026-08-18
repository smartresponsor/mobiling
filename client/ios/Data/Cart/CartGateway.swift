import Foundation

public protocol CartReader: Sendable {
    func currentCart() async throws -> CartMobilePayload
}

public protocol CartWriter: Sendable {
    func addItem(request: CartAddItemRequest) async throws -> CartMobilePayload
}

public protocol CartCheckoutGateway: Sendable {
    func prepareCheckoutHandoff() async throws -> CartCheckoutHandoffPayload
}

public final class CartHttpGateway: CartReader, CartWriter, CartCheckoutGateway, @unchecked Sendable {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func currentCart() async throws -> CartMobilePayload {
        try await request(path: "/cart/current", method: "GET", body: Optional<CartEmptyBody>.none)
    }

    public func addItem(request payload: CartAddItemRequest) async throws -> CartMobilePayload {
        try await request(path: "/cart/item", method: "POST", body: payload)
    }

    public func prepareCheckoutHandoff() async throws -> CartCheckoutHandoffPayload {
        try await request(path: "/cart/checkout-handoff", method: "POST", body: Optional<CartEmptyBody>.none)
    }

    private func request<Response: Decodable, Body: Encodable>(path: String, method: String, body: Body?) async throws -> Response {
        guard let url = URL(string: baseUrl + path) else {
            throw CartGatewayError.invalidBaseUrl
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let body {
            request.httpBody = try JSONEncoder().encode(body)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        } else if method == "POST" {
            request.httpBody = Data()
        }

        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw CartGatewayError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            let apiError = try? JSONDecoder().decode(CartApiError.self, from: data)
            throw CartGatewayError.requestFailed(apiError?.message ?? "Cart request failed.")
        }
        do {
            return try JSONDecoder().decode(Response.self, from: data)
        } catch {
            throw CartGatewayError.invalidPayload
        }
    }
}

private struct CartEmptyBody: Encodable {}

private struct CartApiError: Decodable {
    let code: String?
    let message: String?
}

private enum CartGatewayError: LocalizedError {
    case invalidBaseUrl
    case invalidResponse
    case invalidPayload
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl: return "Cart service URL is invalid."
        case .invalidResponse: return "Cart service returned an invalid response."
        case .invalidPayload: return "Cart data could not be loaded."
        case .requestFailed(let message): return message
        }
    }
