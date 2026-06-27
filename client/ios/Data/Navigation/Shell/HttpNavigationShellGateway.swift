import Foundation

public struct HttpNavigationShellGateway: NavigationShellGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func loadMobileShell() async throws -> MobileNavigationShellPayload {
        guard let url = URL(string: normalizedBaseUrl() + "/navigation/mobile/shell") else {
            throw HttpNavigationShellGatewayError.invalidUrl
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpNavigationShellGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpNavigationShellGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        return try payload(from: data)
    }

    private func payload(from data: Data) throws -> MobileNavigationShellPayload {
        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw HttpNavigationShellGatewayError.invalidPayload
        }

        let locationsObject = object["locations"] as? [String: Any] ?? [:]
        var locations: [String: [MobileNavigationItemPayload]] = [:]

        for (location, rawItems) in locationsObject {
            guard let items = rawItems as? [[String: Any]] else {
                continue
            }

            locations[location] = items.map { item(from: $0) }
        }

        return MobileNavigationShellPayload(
            schema: object["schema"] as? String ?? "smartresponsor.navigation.mobile.shell.v1",
            channel: object["channel"] as? String ?? "mobile",
            platforms: object["platforms"] as? [String] ?? [],
            locations: locations
        )
    }

    private func item(from object: [String: Any]) -> MobileNavigationItemPayload {
        let key = object["key"] as? String ?? ""
        let enabled = object["enabled"] as? Bool ?? false

        return MobileNavigationItemPayload(
            id: key,
            key: key,
            label: object["label"] as? String ?? key,
            icon: object["icon"] as? String,
            badge: object["badge"] as? String,
            enabled: enabled,
            visible: object["visible"] as? Bool ?? true,
            status: object["status"] as? String ?? (enabled ? "active" : "coming_soon"),
            disabledReason: object["disabledReason"] as? String,
            requiredComponent: object["requiredComponent"] as? String,
            location: object["location"] as? String ?? "",
            group: object["group"] as? String ?? "",
            groupLabel: object["groupLabel"] as? String ?? "",
            action: object["action"] as? String,
            route: object["route"] as? String
        )
    }

    private func errorMessage(data: Data, statusCode: Int) -> String {
        guard !data.isEmpty,
              let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return "Mobile navigation shell request failed with HTTP \(statusCode)."
        }

        let code = object["code"] as? String ?? "mobile_navigation_error"
        let message = object["message"] as? String ?? "Mobile navigation shell request failed."

        return "\(code): \(message)"
    }

    private func normalizedBaseUrl() -> String {
        let trimmed = baseUrl.trimmingCharacters(in: .whitespacesAndNewlines)

        return String(trimmed.reversed().drop(while: { $0 == "/" }).reversed())
    }
}

public enum HttpNavigationShellGatewayError: Error {
    case invalidUrl
    case invalidResponse
    case invalidPayload
    case requestFailed(String)
}
