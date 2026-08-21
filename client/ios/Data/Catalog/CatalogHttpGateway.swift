import Foundation

public final class CatalogHttpGateway: CatalogBrowseGateway, CatalogNodeDetailGateway, @unchecked Sendable {
    private let baseUrl: String
    private let catalogCode: String
    private let session: URLSession

    public init(baseUrl: String, catalogCode: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.catalogCode = catalogCode
        self.session = session
    }

    public func listNodes(query: ListCatalogNodesQuery) async throws -> [CatalogNodeSummary] {
        var components = URLComponents(string: baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/catalog")
        var items = [URLQueryItem(name: "catalogCode", value: query.catalogCode?.isEmpty == false ? query.catalogCode : catalogCode)]
        if let parentNodeId = query.parentNodeId, !parentNodeId.isEmpty {
            items.append(URLQueryItem(name: "parentNodeId", value: parentNodeId))
        }
        if let searchText = query.searchText, !searchText.isEmpty {
            items.append(URLQueryItem(name: "q", value: searchText))
        }
        components?.queryItems = items
        guard let url = components?.url else { throw URLError(.badURL) }

        let payload = try await get(url)
        return (payload["nodes"] as? [[String: Any]] ?? []).compactMap(Self.node)
    }

    public func loadNodeDetail(nodeId: String) async throws -> CatalogNodeDetailPayload {
        let encoded = nodeId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? nodeId
        guard let url = URL(string: baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/catalog/node/\(encoded)") else {
            throw URLError(.badURL)
        }
        let payload = try await get(url)
        guard let nodePayload = payload["node"] as? [String: Any], let node = Self.node(nodePayload) else {
            throw URLError(.cannotParseResponse)
        }

        return CatalogNodeDetailPayload(
            node: node,
            description: payload["description"] as? String,
            breadcrumbLabels: payload["breadcrumbLabels"] as? [String] ?? [],
            featuredProductIds: payload["featuredProductIds"] as? [String] ?? []
        )
    }

    private func get(_ url: URL) async throws -> [String: Any] {
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw URLError(.badServerResponse) }
        let payload = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any] ?? [:]
        guard (200..<300).contains(http.statusCode) else {
            throw NSError(
                domain: "Catalog",
                code: http.statusCode,
                userInfo: [NSLocalizedDescriptionKey: payload["message"] as? String ?? "Catalog request failed."]
            )
        }
        return payload
    }

    private static func node(_ payload: [String: Any]) -> CatalogNodeSummary? {
        guard let nodeId = string(payload["nodeId"]), !nodeId.isEmpty else { return nil }
        return CatalogNodeSummary(
            nodeId: nodeId,
            parentNodeId: string(payload["parentNodeId"]),
            title: string(payload["title"]) ?? "Catalog item",
            slug: string(payload["slug"]),
            imageUrl: string(payload["imageUrl"]),
            childCount: integer(payload["childCount"]),
            productCount: integer(payload["productCount"])
        )
    }

    private static func string(_ value: Any?) -> String? {
        if let value = value as? String, !value.isEmpty { return value }
        if let value = value as? NSNumber { return value.stringValue }
        return nil
    }

    private static func integer(_ value: Any?) -> Int {
        if let value = value as? NSNumber { return value.intValue }
        if let value = value as? String, let integer = Int(value) { return integer }
        return 0
    }
}
