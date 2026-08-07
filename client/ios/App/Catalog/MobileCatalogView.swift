import SwiftUI

// Marketing America Corp. Oleksandr Tishchenko
public struct MobileCatalogView: View {
    private let catalogFeatureBridge: CatalogFeatureBridge?

    @State private var nodes: [CatalogNodeSummary] = []
    @State private var navigationStack: [CatalogNodeSummary] = []
    @State private var errorText: String?
    @State private var loading = true
    @State private var reloadKey = UUID()

    public init(catalogFeatureBridge: CatalogFeatureBridge?) {
        self.catalogFeatureBridge = catalogFeatureBridge
    }

    public var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {
                catalogHeader

                if loading {
                    HStack {
                        Spacer()
                        ProgressView()
                            .padding(32)
                        Spacer()
                    }
                } else if let errorText {
                    messageState(
                        title: "We could not load this catalog",
                        message: errorText,
                        actionLabel: "Try again",
                        action: { reloadKey = UUID() }
                    )
                } else if nodes.isEmpty {
                    messageState(
                        title: "Nothing here yet",
                        message: "New categories and listings will appear here as they become available.",
                        actionLabel: navigationStack.isEmpty ? nil : "Back to catalogs",
                        action: { navigationStack.removeAll() }
                    )
                } else {
                    ForEach(nodes, id: \.nodeId) { node in
                        catalogCard(node)
                    }
                }
            }
            .padding(16)
        }
        .task(id: loadIdentity) {
            await loadCatalog()
        }
    }

    private var loadIdentity: String {
        "\(navigationStack.last?.nodeId ?? "root")-\(reloadKey.uuidString)"
    }

    private var catalogHeader: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !navigationStack.isEmpty {
                Button {
                    navigationStack.removeLast()
                } label: {
                    Label("Back", systemImage: "chevron.left")
                }
                .buttonStyle(.plain)
                .foregroundStyle(.tint)
            }

            Text(navigationStack.last?.title ?? "Explore catalogs")
                .font(.title2.weight(.bold))

            Text(
                navigationStack.isEmpty
                    ? "Find work, orders, products, and professional services."
                    : "Choose a category to continue."
            )
            .font(.subheadline)
            .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(.quaternary, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private func catalogCard(_ node: CatalogNodeSummary) -> some View {
        Button {
            guard node.childCount > 0 else {
                return
            }

            navigationStack.append(node)
        } label: {
            HStack(spacing: 14) {
                Text(catalogSymbol(node))
                    .font(.largeTitle)
                    .frame(width: 48, height: 48)

                VStack(alignment: .leading, spacing: 4) {
                    Text(node.title)
                        .font(.headline)
                        .foregroundStyle(.primary)

                    Text(catalogDescription(node))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.leading)

                    Text(catalogCountLabel(node))
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(.tint)
                }

                Spacer(minLength: 8)

                if node.childCount > 0 {
                    Image(systemName: "chevron.right")
                        .foregroundStyle(.tertiary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(.background, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .stroke(.quaternary, lineWidth: 1)
            }
        }
        .buttonStyle(.plain)
        .disabled(node.childCount == 0)
    }

    private func messageState(
        title: String,
        message: String,
        actionLabel: String?,
        action: @escaping () -> Void
    ) -> some View {
        VStack(spacing: 12) {
            Text(title)
                .font(.headline)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            if let actionLabel {
                Button(actionLabel, action: action)
                    .buttonStyle(.borderedProminent)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(28)
    }

    private func loadCatalog() async {
        loading = true
        errorText = nil

        guard let catalogFeatureBridge else {
            nodes = []
            loading = false
            errorText = "Catalog is temporarily unavailable."
            return
        }

        do {
            nodes = try await catalogFeatureBridge.list(
                query: ListCatalogNodesQuery(
                    parentNodeId: navigationStack.last?.nodeId,
                    searchText: nil,
                    includeEmptyNodes: true
                )
            )
            errorText = nil
        } catch {
            nodes = []
            errorText = error.localizedDescription
        }

        loading = false
    }

    private func catalogSymbol(_ node: CatalogNodeSummary) -> String {
        let value = "\(node.title) \(node.slug ?? "")".lowercased()

        if value.contains("task") { return "🧰" }
        if value.contains("order") { return "📋" }
        if value.contains("product") || value.contains("merch") { return "📦" }
        if value.contains("service") { return "🛠️" }
        if value.contains("appliance") { return "🏠" }
        if value.contains("furniture") { return "🪑" }
        if value.contains("repair") { return "🔧" }
        if value.contains("install") { return "⚙️" }

        return "🗂️"
    }

    private func catalogDescription(_ node: CatalogNodeSummary) -> String {
        let value = "\(node.title) \(node.slug ?? "")".lowercased()

        if value.contains("task") {
            return "Customer requests ready for local professionals."
        }
        if value.contains("order") {
            return "Active and packaged work requested by customers."
        }
        if value.contains("product") || value.contains("merch") {
            return "Tools, parts, fixtures, and marketplace goods."
        }
        if value.contains("service") {
            return "Professional services offered by verified vendors."
        }
        if node.childCount > 0 {
            return "Browse \(node.childCount) related categories."
        }

        return "Open this catalog section."
    }

    private func catalogCountLabel(_ node: CatalogNodeSummary) -> String {
        if node.childCount > 0 {
            return "\(node.childCount) categories"
        }
        if let productCount = node.productCount {
            return "\(productCount) listings"
        }

        return "View section"
    }
}
