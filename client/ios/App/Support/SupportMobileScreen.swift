import SwiftUI

public struct SupportMobileScreen: View {
    private let route: String
    private let bridge: SupportFeatureBridge?
    private let onRouteSelected: (String) -> Void

    @State private var page: SupportPagePayload?
    @State private var values: [String: String] = [:]
    @State private var loading = true
    @State private var errorMessage: String?

    public init(route: String, supportFeatureBridge: SupportFeatureBridge?, onRouteSelected: @escaping (String) -> Void) {
        self.route = route
        self.bridge = supportFeatureBridge
        self.onRouteSelected = onRouteSelected
    }

    public var body: some View {
        List {
            Section {
                Text(page?.title ?? "Support").font(.title2).fontWeight(.semibold)
                if let description = page?.description, !description.isEmpty {
                    Text(description).foregroundStyle(.secondary)
                }
            }

            if loading {
                ProgressView("Loading…")
            } else if let errorMessage {
                Text(errorMessage).foregroundStyle(.red)
            } else if let page {
                ForEach(page.rows) { row in
                    Button {
                        onRouteSelected(normalize(row.href))
                    } label: {
                        VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                            Text("\(row.context) · \(row.request)").font(.headline)
                            if !row.description.isEmpty { Text(row.description).foregroundStyle(.primary) }
                            Text("\(row.availableItems) available").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }

                ForEach(page.cases) { item in
                    Button {
                        onRouteSelected(normalize(item.href))
                    } label: {
                        VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                            Text(item.reference).font(.headline)
                            Text(item.status).font(.subheadline).foregroundStyle(.tint)
                            Text("\(item.context) · \(item.category)").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }

                if let reference = page.reference {
                    Section("Case") {
                        Text(reference).font(.headline)
                        if let status = page.status { Text(status).foregroundStyle(.tint) }
                        if let businessContext = page.businessContext { Text(businessContext) }
                        if let category = page.category { Text(category).font(.caption).foregroundStyle(.secondary) }
                        if let descriptionText = page.descriptionText { Text(descriptionText) }
                    }
                }

                if let informationQuestion = page.informationQuestion {
                    Section("Information requested") {
                        Text(informationQuestion)
                    }
                }

                if !page.fields.isEmpty {
                    Section("Your response") {
                        ForEach(page.fields) { field in
                            supportField(field)
                        }
                        if let action = page.action {
                            Button("Continue") {
                                Task { await execute(path: action, method: page.method, fields: values) }
                            }
                            .buttonStyle(.borderedProminent)
                        }
                    }
                }

                if !page.actions.isEmpty {
                    Section {
                        ForEach(page.actions.filter(\.enabled)) { action in
                            Button(action.label) {
                                Task { await execute(path: action.href, method: action.method, fields: [:]) }
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle(route == "support/case" ? "My cases" : "Support")
        .task(id: route) { await load() }
    }

    @ViewBuilder
    private func supportField(_ field: SupportFieldPayload) -> some View {
        if field.options.isEmpty {
            if field.type == "textarea" {
                VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                    Text(field.label).font(.caption).foregroundStyle(.secondary)
                    TextEditor(text: binding(for: field)).frame(minHeight: 100)
                }
            } else {
                TextField(field.label, text: binding(for: field))
            }
        } else {
            Picker(field.label, selection: binding(for: field)) {
                Text("Select").tag("")
                ForEach(field.options) { option in
                    Text(option.label).tag(option.value)
                }
            }
        }
    }

    private func binding(for field: SupportFieldPayload) -> Binding<String> {
        Binding(
            get: { values[field.name] ?? field.value ?? "" },
            set: { values[field.name] = $0 }
        )
    }

    @MainActor
    private func load() async {
        loading = true
        errorMessage = nil
        guard let bridge else {
            loading = false
            errorMessage = "Support service is not available."
            return
        }
        do {
            apply(try await bridge.load(path: "/" + route.trimmingCharacters(in: CharacterSet(charactersIn: "/"))))
        } catch {
            loading = false
            errorMessage = error.localizedDescription
        }
    }

    @MainActor
    private func execute(path: String, method: String, fields: [String: String]) async {
        guard let bridge else { return }
        if method.uppercased() != "POST" {
            onRouteSelected(normalize(path))
            return
        }
        loading = true
        do {
            apply(try await bridge.submit(path: path, fields: fields))
        } catch {
            loading = false
            errorMessage = error.localizedDescription
        }
    }

    @MainActor
    private func apply(_ next: SupportPagePayload) {
        page = next
        values = Dictionary(uniqueKeysWithValues: next.fields.compactMap { field in
            field.value.map { (field.name, $0) }
        })
        loading = false
        errorMessage = nil
    }

    private func normalize(_ path: String) -> String {
        path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }
}
