import SwiftUI

// Marketing America Corp. Oleksandr Tishchenko
private struct CatalogReferenceView: View {
    private let catalogFeatureBridge: CatalogFeatureBridge?
    @State private var nodes: [CatalogNodeSummary] = []
    @State private var errorText: String?

    public init(catalogFeatureBridge: CatalogFeatureBridge?) {
        self.catalogFeatureBridge = catalogFeatureBridge
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Catalog")
                .font(.headline)
            if let errorText {
                Text(errorText)
            }
            if nodes.isEmpty && errorText == nil {
                Text("No catalog nodes are available.")
            }
            ForEach(nodes, id: \.nodeId) { node in
                VStack(alignment: .leading, spacing: 4) {
                    Text(node.title)
                    Text(node.slug ?? node.nodeId)
                        .font(.caption)
                }
                .padding(.vertical, 4)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .task {
            await loadCatalog()
        }
    }

    private func loadCatalog() async {
        guard let catalogFeatureBridge else {
            errorText = "Catalog service is not available."
            nodes = []
            return
        }

        do {
            nodes = try await catalogFeatureBridge.list(query: ListCatalogNodesQuery(parentNodeId: nil, searchText: nil, includeEmptyNodes: true))
            errorText = nil
        } catch {
            nodes = []
            errorText = error.localizedDescription
        }
    }
}
        }
