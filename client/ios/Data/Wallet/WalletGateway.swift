import Foundation

public struct WalletCurrencyBalance: Decodable, Sendable {
    public let code: String
    public let availableMinor: Int64
    public let reservedMinor: Int64
    public let totalMinor: Int64
}

public struct WalletBalancePayload: Decodable, Sendable {
    public let walletId: String?
    public let currency: [WalletCurrencyBalance]
}

public struct WalletTransactionItem: Decodable, Identifiable, Sendable {
    public var id: String { transactionId }
    public let transactionId: String
    public let type: String
    public let amountMinor: Int64
    public let currency: String
    public let postedAt: String
}

public struct WalletOperationItem: Decodable, Identifiable, Sendable {
    public let id: String
    public let type: String
    public let status: String
    public let amountMinor: Int64
    public let currency: String
    public let transactionId: String?
    public let reversalTransactionId: String?
    public let sourceType: String?
    public let sourceId: String?
    public let sourceReference: String?
    public let destinationReference: String?
    public let railReference: String?
}

public struct WalletWithdrawalDestination: Decodable, Identifiable, Sendable {
    public let id: String
    public let type: String
    public let label: String
}

public protocol WalletGateway {
    func loadBalance() async throws -> WalletBalancePayload
    func loadTransactions() async throws -> [WalletTransactionItem]
    func loadFunding() async throws -> [WalletOperationItem]
    func loadWithdrawals() async throws -> [WalletOperationItem]
    func loadWithdrawal(id: String) async throws -> WalletOperationItem
    func loadWithdrawalDestinations() async throws -> [WalletWithdrawalDestination]
    func requestWithdrawal(amountMinor: Int64, currency: String, paymentInstrumentId: String, idempotencyKey: String) async throws -> WalletOperationItem
    func cancelWithdrawal(id: String) async throws -> WalletOperationItem
}

public struct WalletHttpGateway: WalletGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func loadBalance() async throws -> WalletBalancePayload {
        let response: WalletDataEnvelope<WalletBalancePayload> = try await request(path: "/wallet/balance", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data
    }

    public func loadTransactions() async throws -> [WalletTransactionItem] {
        let response: WalletDataEnvelope<WalletItemEnvelope<WalletTransactionItem>> = try await request(path: "/wallet/transaction", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data.item
    }

    public func loadFunding() async throws -> [WalletOperationItem] {
        let response: WalletDataEnvelope<WalletItemEnvelope<WalletOperationItem>> = try await request(path: "/wallet/funding", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data.item
    }

    public func loadWithdrawals() async throws -> [WalletOperationItem] {
        let response: WalletDataEnvelope<WalletItemEnvelope<WalletOperationItem>> = try await request(path: "/wallet/withdrawal", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data.item
    }

    public func loadWithdrawal(id: String) async throws -> WalletOperationItem {
        let encoded = id.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? id
        let response: WalletDataEnvelope<WalletOperationItem> = try await request(path: "/wallet/withdrawal/\(encoded)", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data
    }

    public func loadWithdrawalDestinations() async throws -> [WalletWithdrawalDestination] {
        let response: WalletDataEnvelope<WalletItemEnvelope<WalletWithdrawalDestination>> = try await request(path: "/wallet/withdrawal/destination", method: "GET", body: Optional<WalletEmptyBody>.none)
        return response.data.item
    }

    public func requestWithdrawal(amountMinor: Int64, currency: String, paymentInstrumentId: String, idempotencyKey: String) async throws -> WalletOperationItem {
        let response: WalletDataEnvelope<WalletOperationItem> = try await request(
            path: "/wallet/withdrawal/request",
            method: "POST",
            body: WalletWithdrawalRequestBody(amountMinor: amountMinor, currency: currency, paymentInstrumentId: paymentInstrumentId, idempotencyKey: idempotencyKey)
        )
        return response.data
    }

    public func cancelWithdrawal(id: String) async throws -> WalletOperationItem {
        let encoded = id.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? id
        let response: WalletDataEnvelope<WalletOperationItem> = try await request(path: "/wallet/withdrawal/cancel/\(encoded)", method: "POST", body: WalletEmptyBody())
        return response.data
    }

    private func request<Response: Decodable, Body: Encodable>(path: String, method: String, body: Body?) async throws -> Response {
        guard let url = URL(string: baseUrl + path) else { throw WalletGatewayError.invalidBaseUrl }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONEncoder().encode(body)
        }
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw WalletGatewayError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let apiError = try? JSONDecoder().decode(WalletApiError.self, from: data)
            let message = apiError?.message ?? apiError?.code?.replacingOccurrences(of: "_", with: " ") ?? "Wallet request failed with HTTP \(http.statusCode)."
            throw WalletGatewayError.requestFailed(message)
        }
        do {
            return try JSONDecoder().decode(Response.self, from: data)
        } catch {
            throw WalletGatewayError.invalidPayload(error.localizedDescription)
        }
    }
}

private struct WalletDataEnvelope<Value: Decodable>: Decodable { let data: Value }
private struct WalletItemEnvelope<Value: Decodable>: Decodable { let item: [Value] }
private struct WalletEmptyBody: Encodable {}
private struct WalletWithdrawalRequestBody: Encodable {
    let amountMinor: Int64
    let currency: String
    let paymentInstrumentId: String
    let idempotencyKey: String
}
private struct WalletApiError: Decodable { let code: String?; let message: String? }

private enum WalletGatewayError: LocalizedError {
    case invalidBaseUrl
    case invalidResponse
    case invalidPayload(String)
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl: return "Wallet gateway URL is invalid."
        case .invalidResponse: return "Wallet gateway returned an invalid response."
        case .invalidPayload(let message): return "Wallet response could not be decoded: \(message)"
        case .requestFailed(let message): return message
        }
    }
}