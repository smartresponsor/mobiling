import Foundation

public struct PushTokenRegistrar {
    private let baseUrl: String

    public init(baseUrl: String = "https://httpbin.org") {
        self.baseUrl = baseUrl
    }

    public func register(payload: PushRegistrationPayload) async throws -> Bool {
        let url = URL(string: baseUrl + "/anything/mobile/push/token?platform=\(payload.platform)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = payload.token.data(using: .utf8)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }
}
