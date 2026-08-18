import Foundation

public struct PushTokenRegistrar {
    private let baseUrl: String

    public init(baseUrl: String) {
        self.baseUrl = baseUrl
    }

    public func register(payload: PushRegistrationPayload) async throws -> Bool {
        guard let url = URL(string: baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/notification/subscription") else {
            return false
        }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.httpBody = try JSONEncoder().encode(payload)
        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else { return false }
        return (200..<300).contains(http.statusCode)
    }
}
