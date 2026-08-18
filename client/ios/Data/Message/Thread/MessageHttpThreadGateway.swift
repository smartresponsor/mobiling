import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct MessageHttpThreadGateway: MessageThreadGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func listThreads() async throws -> [MessageThreadSummary] {
        let response: ThreadListResponse = try await request(path: "/message/thread", method: "GET", body: Optional<EmptyBody>.none)
        return response.items
    }

    public func listItems(threadId: String) async throws -> [MessageItemPayload] {
        let encoded = threadId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? threadId
        let response: MessageListResponse = try await request(path: "/message/thread/\(encoded)", method: "GET", body: Optional<EmptyBody>.none)
        return response.items.sorted { $0.sentAtIso8601 < $1.sentAtIso8601 }
    }

    public func sendMessage(request payload: SendMessageRequest) async throws -> MessageItemPayload {
        let encoded = payload.threadId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? payload.threadId
        return try await request(path: "/message/thread/\(encoded)/send", method: "POST", body: SendBody(body: payload.body))
    }

    public func markRead(threadId: String, messageId: String) async throws {
        let encoded = threadId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? threadId
        let _: ReadResponse = try await request(path: "/message/thread/\(encoded)/read", method: "POST", body: ReadBody(messageId: messageId))
    }

    private func request<Response: Decodable, Body: Encodable>(path: String, method: String, body: Body?) async throws -> Response {
        guard let url = URL(string: baseUrl + path) else { throw MessageGatewayError.invalidBaseUrl }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONEncoder().encode(body)
        }

        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw MessageGatewayError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let error = try? JSONDecoder().decode(ErrorResponse.self, from: data)
            throw MessageGatewayError.requestFailed(error?.message ?? "Messaging request failed with HTTP \(http.statusCode).")
        }
        return try JSONDecoder().decode(Response.self, from: data)
    }
}

private struct ThreadListResponse: Decodable { let items: [MessageThreadSummary] }
private struct MessageListResponse: Decodable { let items: [MessageItemPayload] }
private struct SendBody: Encodable { let body: String }
private struct ReadBody: Encodable { let messageId: String }
private struct ReadResponse: Decodable { let ok: Bool? }
private struct EmptyBody: Encodable {}
private struct ErrorResponse: Decodable { let message: String? }

private enum MessageGatewayError: LocalizedError {
    case invalidBaseUrl
    case invalidResponse
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidBaseUrl: return "Messaging gateway URL is invalid."
        case .invalidResponse: return "Messaging gateway returned an invalid response."
        case .requestFailed(let message): return message
        }
    }
}
