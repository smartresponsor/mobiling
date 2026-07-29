import Foundation
import SwiftUI

public protocol VendorCrudGateway: Sendable {
    func list(resource: String) async throws -> [[String: String]]
    func create(resource: String, fields: [String: String]) async throws
    func update(resource: String, identity: String, fields: [String: String]) async throws
    func delete(resource: String, identity: String) async throws
}

public final class HttpVendorCrudGateway: VendorCrudGateway, @unchecked Sendable {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func list(resource: String) async throws -> [[String: String]] {
        let payload = try await request(method: "GET", resource: resource, identity: nil, fields: nil)
        return (payload["items"] as? [[String: Any]] ?? []).map(Self.strings)
    }

    public func create(resource: String, fields: [String: String]) async throws {
        _ = try await request(method: "POST", resource: resource, identity: nil, fields: fields)
    }

    public func update(resource: String, identity: String, fields: [String: String]) async throws {
        _ = try await request(method: "PATCH", resource: resource, identity: identity, fields: fields)
    }

    public func delete(resource: String, identity: String) async throws {
        _ = try await request(method: "DELETE", resource: resource, identity: identity, fields: nil)
    }

    private func request(method: String, resource: String, identity: String?, fields: [String: String]?) async throws -> [String: Any] {
        let suffix = identity.map { "/\($0.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? $0)" } ?? ""
        guard let url = URL(string: baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/crud/my/\(resource)\(suffix)") else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let fields {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: fields)
        }
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw URLError(.badServerResponse) }
        let payload = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any] ?? [:]
        guard (200..<300).contains(http.statusCode) else {
            throw NSError(domain: "VendorCrud", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: payload["message"] as? String ?? "CRUD request failed."])
        }
        return payload
    }

    private static func strings(_ source: [String: Any]) -> [String: String] {
        source.reduce(into: [:]) { result, entry in
            if let value = entry.value as? String { result[entry.key] = value }
            else if let value = entry.value as? NSNumber { result[entry.key] = value.stringValue }
        }
    }
}

public struct VendorOwnedCrudView: View {
    let title: String
    let resource: String
    let routeRoot: String
    let selectedId: String?
    let gateway: VendorCrudGateway?
    let onRouteSelected: (String) -> Void

    @State private var items: [[String: String]] = []
    @State private var value = ""
    @State private var error: String?
    @State private var saving = false
    @State private var reloadToken = 0

    public var body: some View {
        List {
            Section(selectedId == nil ? "My \(title)" : "\(title.dropLast()) Detail") {
                TextField(resource == "order" ? "Reference" : "Name", text: $value)
                if let error { Text(error).foregroundStyle(.red) }
                if let selectedId {
                    Button("Save changes") { mutate { try await gateway?.update(resource: resource, identity: selectedId, fields: fields(value)) } }
                    Button("Delete", role: .destructive) {
                        mutate {
                            try await gateway?.delete(resource: resource, identity: selectedId)
                            onRouteSelected(routeRoot)
                        }
                    }
                    Button("Back to \(title)") { onRouteSelected(routeRoot) }
                } else {
                    Button("New \(title.dropLast())") { onRouteSelected("\(routeRoot)/new") }
                }
            }
            Section(title) {
                ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                    let identity = item["id"] ?? item["\(resource)Id"] ?? ""
                    Button(label(item)) { onRouteSelected("\(routeRoot)/\(identity)") }
                        .disabled(identity.isEmpty)
                }
            }
        }
        .disabled(saving)
        .navigationTitle(selectedId == nil ? "My \(title)" : "\(title.dropLast()) Detail")
        .task(id: reloadToken) { await load() }
    }

    private func load() async {
        do {
            items = try await gateway?.list(resource: resource) ?? []
            if let selectedId, let selected = items.first(where: { ($0["id"] ?? $0["\(resource)Id"]) == selectedId }) {
                value = label(selected)
            }
            error = nil
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func mutate(_ operation: @escaping () async throws -> Void) {
        guard !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            error = "Name is required."
            return
        }
        Task {
            saving = true
            do {
                try await operation()
                error = nil
                reloadToken += 1
            } catch {
                self.error = error.localizedDescription
            }
            saving = false
        }
    }

    private func fields(_ value: String) -> [String: String] {
        resource == "order" ? ["reference": value, "number": value] : ["title": value, "name": value]
    }

    private func label(_ item: [String: String]) -> String {
        item["title"] ?? item["name"] ?? item["reference"] ?? item["number"] ?? item["id"] ?? title.dropLast()
    }
}
