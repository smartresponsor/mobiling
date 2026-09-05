import Foundation

public struct NotificationHttpGateway: NotificationGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func inbox() async throws -> NotificationInboxPayload {
        try await request(path: "/notification", method: "GET", body: Optional<EmptyBody>.none)
    }

    public func unreadCount() async throws -> Int {
        let payload: UnreadPayload = try await request(path: "/notification/unread/count", method: "GET", body: Optional<EmptyBody>.none)
        return payload.unreadCount
    }

    public func markRead(ids: [String]) async throws -> Int {
        let payload: UnreadPayload = try await request(path: "/notification/mark/read", method: "POST", body: MarkReadBody(ids: ids))
        return payload.unreadCount
    }

    public func subscription(request payload: NotificationSubscriptionRequest) async throws -> Bool {
        let response: SubscriptionPayload = try await request(path: "/notification/subscription", method: "POST", body: payload)
        return response.ok
    }

    private func request<Response: Decodable, Body: Encodable>(path: String, method: String, body: Body?) async throws -> Response {
        guard let url = URL(string: baseUrl + path) else { throw NotificationGatewayError.invalidBaseUrl }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONEncoder().encode(body)
        }
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw NotificationGatewayError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let error = try? JSONDecoder().decode(ErrorPayload.self, from: data)
            throw NotificationGatewayError.requestFailed(error?.message ?? "Notification request failed with HTTP \(http.statusCode).")
        }
        return try JSONDecoder().decode(Response.self, from: data)
    }
}

private struct UnreadPayload: Decodable { let unreadCount: Int }
private struct SubscriptionPayload: Decodable { let ok: Bool }
private struct MarkReadBody: Encodable { let ids: [String] }
private struct EmptyBody: Encodable {}
private struct ErrorPayload: Decodable { let message: String? }

private enum NotificationGatewayError: LocalizedError {
    case invalidBaseUrl
    case invalidResponse
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl: return "Notification gateway URL is invalid."
        case .invalidResponse: return "Notification gateway returned an invalid response."
        case .requestFailed(let message): return message
        }
    }
}
