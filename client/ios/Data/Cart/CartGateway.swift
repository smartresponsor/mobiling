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

public protocol CartTokenStore: Sendable {
    func current() -> String?
    func save(_ token: String)
}

public final class UserDefaultsCartTokenStore: CartTokenStore, @unchecked Sendable {
    private let defaults: UserDefaults
    private let key = "mobiling.cart.token"

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    public func current() -> String? {
        guard let value = defaults.string(forKey: key)?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else {
            return nil
        }
        return value
    }

    public func save(_ token: String) {
        let normalized = token.trimmingCharacters(in: .whitespacesAndNewlines)
        if normalized.isEmpty {
            defaults.removeObject(forKey: key)
        } else {
            defaults.set(normalized, forKey: key)
        }
    }
}

public final class CartHttpGateway: CartReader, CartWriter, CartCheckoutGateway, @unchecked Sendable {
    private let baseUrl: String
    private let session: URLSession
    private let tokenStore: any CartTokenStore

    public init(baseUrl: String, session: URLSession = .shared, tokenStore: any CartTokenStore = UserDefaultsCartTokenStore()) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
        self.tokenStore = tokenStore
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
        if let cartToken = tokenStore.current() {
            request.setValue(cartToken, forHTTPHeaderField: "X-Cart-Token")
        }
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
        if let cartToken = http.value(forHTTPHeaderField: "X-Cart-Token") {
            tokenStore.save(cartToken)
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
