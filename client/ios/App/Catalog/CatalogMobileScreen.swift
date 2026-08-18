import SwiftUI

public struct CatalogMobileScreen: View {
    private let bridge: CatalogFeatureBridge?

    @State private var nodes: [CatalogNodeSummary] = []
    @State private var path: [CatalogNodeSummary] = []
    @State private var loading = true
    @State private var errorMessage: String?

    public init(catalogFeatureBridge: CatalogFeatureBridge?) {
        self.bridge = catalogFeatureBridge
    }

    public var body: some View {
        List {
            if let parent = path.last {
                Section {
                    Button {
                        path.removeLast()
                    } label: {
                        Label(path.count == 1 ? "Services" : path[path.count - 2].title, systemImage: "chevron.left")
                    }
                    Text(parent.title)
                        .font(.headline)
                }
            }

            if loading {
                ProgressView("Loading catalog…")
            } else if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.secondary)
            } else if nodes.isEmpty {
                Text("No catalog nodes are available.")
                    .foregroundStyle(.secondary)
            } else {
                Section {
                    ForEach(nodes, id: \.nodeId) { node in
                        Button {
                            guard node.childCount > 0 else { return }
                            path.append(node)
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                                    Text(node.title)
                                        .foregroundStyle(.primary)
                                    Text(summary(for: node))
                                        .font(.footnote)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                if node.childCount > 0 {
                                    Image(systemName: "chevron.right")
                                        .foregroundStyle(.tertiary)
                                }
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .navigationTitle("Catalog")
        .task(id: path.last?.nodeId) {
            await load()
        }
    }

    @MainActor private func load() async {
        guard let bridge else {
            loading = false
            errorMessage = "Catalog service is not available."
            return
        }

        loading = true
        do {
            nodes = try await bridge.list(
                query: ListCatalogNodesQuery(
                    parentNodeId: path.last?.nodeId,
                    searchText: nil,
                    includeEmptyNodes: true
                )
            )
            errorMessage = nil
        } catch {
            nodes = []
            errorMessage = error.localizedDescription
        }
        loading = false
    }

    private func summary(for node: CatalogNodeSummary) -> String {
        if node.childCount > 0 {
            return "\(node.childCount) service types"
        }
        if let count = node.productCount, count > 0 {
            return "\(count) services"
        }
        return node.slug ?? "Service type"
    }
}
