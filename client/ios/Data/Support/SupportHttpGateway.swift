import Foundation

public protocol SupportGateway: Sendable {
    func load(path: String) async throws -> SupportPagePayload
    func submit(path: String, fields: [String: String]) async throws -> SupportPagePayload
}

public struct SupportHttpGateway: SupportGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    public func load(path: String) async throws -> SupportPagePayload {
        try await request(path: path, method: "GET", fields: nil)
    }

    public func submit(path: String, fields: [String: String]) async throws -> SupportPagePayload {
        try await request(path: path, method: "POST", fields: fields)
    }

    private func request(path: String, method: String, fields: [String: String]?) async throws -> SupportPagePayload {
        guard path.hasPrefix("/support") else { throw SupportGatewayError.invalidPath }
        guard let url = URL(string: baseUrl + path) else { throw SupportGatewayError.invalidUrl }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let fields {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: fields)
        }

        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw SupportGatewayError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let object = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any]
            throw SupportGatewayError.requestFailed(object?["message"] as? String ?? "Support request failed with HTTP \(http.statusCode).")
        }
        let root = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        return parse(root)
    }

    private func parse(_ root: [String: Any]) -> SupportPagePayload {
        let data = root["data"] as? [String: Any] ?? [:]
        let meta = root["meta"] as? [String: Any] ?? [:]
        let locations = (root["interface"] as? [String: Any])?["locations"] as? [String: Any]
        let content = (locations?["shell.main.content"] as? [[String: Any]])?.first
        let rowDictionaries = data["rows"] as? [[String: Any]] ?? []

        let rows = rowDictionaries.compactMap { row -> SupportRowPayload? in
            guard row["availableItems"] != nil || row["request"] != nil else { return nil }
            return SupportRowPayload(
                id: string(row["id"]),
                context: string(row["context"]),
                request: string(row["request"]),
                description: string(row["description"]),
                href: string(row["href"]),
                availableItems: integer(row["availableItems"])
            )
        }
        let cases = rowDictionaries.compactMap { row -> CaseRowPayload? in
            guard row["reference"] != nil, row["availableItems"] == nil else { return nil }
            return CaseRowPayload(
                reference: string(row["reference"]),
                context: string(row["context"]),
                category: string(row["category"]),
                status: string(row["status"]),
                href: string(row["href"])
            )
        }

        let informationForm = data["informationForm"] as? [String: Any]
        let fieldDictionaries = data["formFields"] as? [[String: Any]]
            ?? informationForm?["fields"] as? [[String: Any]]
            ?? []
        let fields = fieldDictionaries.map { field in
            SupportFieldPayload(
                name: string(field["nameEntity"]),
                label: string(field["label"]),
                type: string(field["type"], fallback: "text"),
                value: optionalString(field["value"]),
                required: boolean(field["required"]),
                options: (field["options"] as? [[String: Any]] ?? []).map {
                    SupportOptionPayload(label: string($0["label"]), value: string($0["value"]))
                }
            )
        }
        let actions = (data["headerActions"] as? [[String: Any]] ?? []).map {
            SupportActionPayload(
                label: string($0["label"]),
                href: string($0["href"]),
                method: string($0["method"], fallback: "GET"),
                enabled: ($0["enabled"] as? Bool) ?? true
            )
        }

        return SupportPagePayload(
            title: string(meta["title"], fallback: string(content?["label"], fallback: "Support")),
            description: string(content?["description"]),
            rows: rows,
            cases: cases,
            fields: fields,
            actions: actions,
            action: optionalString(data["action"]) ?? optionalString(informationForm?["action"]),
            method: string(data["method"], fallback: string(informationForm?["method"], fallback: "GET")),
            reference: optionalString(data["reference"]) ?? optionalString(data["caseReference"]),
            status: optionalString(data["status"]),
            businessContext: optionalString(data["businessContext"]),
            category: optionalString(data["category"]),
            descriptionText: optionalString(data["description"])
        )
    }

    private func string(_ value: Any?, fallback: String = "") -> String {
        if let value = value as? String { return value }
        if let value { return String(describing: value) }
        return fallback
    }

    private func optionalString(_ value: Any?) -> String? {
        let value = string(value)
        return value.isEmpty ? nil : value
    }

    private func integer(_ value: Any?) -> Int {
        if let value = value as? Int { return value }
        if let value = value as? NSNumber { return value.intValue }
        return Int(string(value)) ?? 0
    }

    private func boolean(_ value: Any?) -> Bool {
        if let value = value as? Bool { return value }
        if let value = value as? NSNumber { return value.boolValue }
        return ["1", "true", "yes"].contains(string(value).lowercased())
    }
}

private enum SupportGatewayError: LocalizedError {
    case invalidPath
    case invalidUrl
    case invalidResponse
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidPath: return "Support path is invalid."
        case .invalidUrl: return "Support gateway URL is invalid."
        case .invalidResponse: return "Support gateway returned an invalid response."
        case .requestFailed(let message): return message
        }
    }
}
