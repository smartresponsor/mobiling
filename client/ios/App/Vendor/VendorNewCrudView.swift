import SwiftUI
import WebKit

public struct VendorNewField: Hashable, Sendable {
    let key: String
    let label: String
    let required: Bool
    let numeric: Bool

    init(_ key: String, _ label: String, required: Bool = false, numeric: Bool = false) {
        self.key = key
        self.label = label
        self.required = required
        self.numeric = numeric
    }
}

private let RetailCatalogChoices: [(value: String, label: String)] = [
    ("services", "Services"),
    ("products", "Products"),
    ("projects", "Projects"),
]

private func retailCatalogCode(_ kind: String) -> String {
    switch kind {
    case RetailKind.goods.rawValue: return "products"
    case RetailKind.project.rawValue: return "projects"
    default: return "services"
    }
}

private struct RetailCategoryChoice: Identifiable, Hashable {
    let id: String
    let label: String
}

private func loadRetailCategoryChoices(
    bridge: CatalogFeatureBridge,
    catalogCode: String,
    parentNodeId: String? = nil,
    prefix: String = ""
) async throws -> [RetailCategoryChoice] {
    let nodes = try await bridge.list(query: ListCatalogNodesQuery(
        parentNodeId: parentNodeId,
        searchText: nil,
        includeEmptyNodes: true,
        catalogCode: catalogCode
    ))

    var choices: [RetailCategoryChoice] = []
    for node in nodes {
        let label = prefix.isEmpty ? node.title : "\(prefix) › \(node.title)"
        choices.append(RetailCategoryChoice(id: node.nodeId, label: label))
        if node.childCount > 0 {
            choices.append(contentsOf: try await loadRetailCategoryChoices(
                bridge: bridge,
                catalogCode: catalogCode,
                parentNodeId: node.nodeId,
                prefix: label
            ))
        }
    }
    return choices
}

public struct VendorNewCrudView: View {
    let singular: String
    let resource: String
    let listRoute: String
    let fields: [VendorNewField]
    let gateway: VendorCrudGateway?
    let catalogFeatureBridge: CatalogFeatureBridge?
    let onRouteSelected: (String) -> Void
    let availableRetailKinds: [RetailKind]

    @State private var values: [String: String]
    @State private var fieldErrors: [String: String] = [:]
    @State private var submitError: String?
    @State private var saving = false
    @State private var categoryChoices: [RetailCategoryChoice] = []
    @State private var categoryLoading = false
    @State private var categoryLoadError: String?

    public init(singular: String, resource: String, listRoute: String, fields: [VendorNewField], gateway: VendorCrudGateway?, catalogFeatureBridge: CatalogFeatureBridge? = nil, onRouteSelected: @escaping (String) -> Void, initialValues: [String: String] = [:], availableRetailKinds: [RetailKind] = RetailKind.allCases) {
        self.singular = singular
        self.resource = resource
        self.listRoute = listRoute
        self.fields = fields
        self.gateway = gateway
        self.catalogFeatureBridge = catalogFeatureBridge
        self.onRouteSelected = onRouteSelected
        self.availableRetailKinds = availableRetailKinds
        var normalizedValues = fields.reduce(into: initialValues) { values, field in
            if values[field.key] == nil { values[field.key] = "" }
        }
        if fields.contains(where: { $0.key == "catalogCode" }), normalizedValues["catalogCode", default: ""].isEmpty {
            normalizedValues["catalogCode"] = retailCatalogCode(normalizedValues["kind", default: ""])
        }
        _values = State(initialValue: normalizedValues)
    }

    public var body: some View {
        Form {
            Section("Details") {
                ForEach(fields, id: \.key) { field in
                    VStack(alignment: .leading, spacing: 4) {
                        if field.key == "kind" {
                            Picker("Listing type", selection: binding(field.key)) {
                                ForEach(availableRetailKinds, id: \.rawValue) { kind in
                                    Text(retailKindOptionLabel(kind)).tag(kind.rawValue)
                                }
                            }
                            .pickerStyle(.inline)
                        } else if field.key == "catalogCode" {
                            Picker("Catalog", selection: binding(field.key)) {
                                ForEach(RetailCatalogChoices, id: \.value) { catalog in
                                    Text(catalog.label).tag(catalog.value)
                                }
                            }
                            .pickerStyle(.inline)
                        } else if field.key == "categoryId" {
                            if categoryLoading {
                                ProgressView("Loading categories…")
                            } else if let categoryLoadError {
                                Text(categoryLoadError).font(.caption).foregroundStyle(.red)
                            } else if categoryChoices.isEmpty {
                                Text("No published categories are available for this catalog.")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            } else {
                                Picker("Category", selection: binding(field.key)) {
                                    Text("Choose a category").tag("")
                                    ForEach(categoryChoices) { category in
                                        Text(category.label).tag(category.id)
                                    }
                                }
                                .pickerStyle(.navigationLink)
                            }
                        } else {
                            TextField(field.label + (field.required ? " *" : ""), text: binding(field.key), axis: field.key == "description" ? .vertical : .horizontal)
                                .keyboardType(field.numeric ? .decimalPad : .default)
                        }
                        if let message = fieldErrors[field.key] {
                            Text(message).font(.caption).foregroundStyle(.red)
                        }
                    }
                }
            }
            if let submitError {
                Section { Text(submitError).foregroundStyle(.red) }
            }
            Section {
                Button(saving ? "Creating…" : "Create \(singular)") { submit() }
                    .disabled(saving)
                Button("Cancel", role: .cancel) { onRouteSelected(listRoute) }
            }
        }
        .navigationTitle("New \(singular)")
        .task(id: values["catalogCode", default: ""]) {
            await loadCategories()
        }
    }

    private func retailKindOptionLabel(_ kind: RetailKind) -> String {
        switch kind {
        case .task: return "Task — I need something done"
        case .service: return "Service — I offer my skills"
        case .goods: return "Product — I am selling an item"
        case .project: return "Project — I am publishing a project"
        }
    }

    private func binding(_ key: String) -> Binding<String> {
        Binding(
            get: { values[key, default: ""] },
            set: { newValue in
                let previousKind = values["kind", default: ""]
                let previousCatalog = values["catalogCode", default: ""]
                values[key] = newValue
                if key == "kind", previousCatalog.isEmpty || previousCatalog == retailCatalogCode(previousKind) {
                    let nextCatalog = retailCatalogCode(newValue)
                    if nextCatalog != previousCatalog {
                        values["catalogCode"] = nextCatalog
                        values["categoryId"] = ""
                    }
                } else if key == "catalogCode", newValue != previousCatalog {
                    values["categoryId"] = ""
                }
                fieldErrors[key] = nil
            }
        )
    }

    @MainActor
    private func loadCategories() async {
        guard fields.contains(where: { $0.key == "categoryId" }) else {
            return
        }
        guard let catalogFeatureBridge else {
            categoryChoices = []
            categoryLoadError = "Catalog is not available."
            return
        }

        let catalogCode = values["catalogCode", default: ""]
        guard !catalogCode.isEmpty else {
            categoryChoices = []
            categoryLoadError = nil
            return
        }

        categoryLoading = true
        categoryLoadError = nil
        do {
            categoryChoices = try await loadRetailCategoryChoices(
                bridge: catalogFeatureBridge,
                catalogCode: catalogCode
            )
        } catch {
            categoryChoices = []
            categoryLoadError = error.localizedDescription
        }
        categoryLoading = false
    }

    private func submit() {
        var errors: [String: String] = [:]
        for field in fields {
            let value = values[field.key, default: ""].trimmingCharacters(in: .whitespacesAndNewlines)
            if field.required && value.isEmpty { errors[field.key] = "\(field.label) is required." }
            if field.numeric && !value.isEmpty && Decimal(string: value) == nil { errors[field.key] = "\(field.label) must be a number." }
        }
        fieldErrors = errors
        guard errors.isEmpty else { return }

        Task {
            saving = true
            submitError = nil
            do {
                let payload = values.mapValues { $0.trimmingCharacters(in: .whitespacesAndNewlines) }.filter { !$0.value.isEmpty }
                try await gateway?.create(resource: resource, fields: payload)
                onRouteSelected(listRoute)
            } catch {
                submitError = error.localizedDescription
            }
            saving = false
        }
    }
}

private struct ProjectStoryRichTextEditor: View {
    @Binding var documentJson: String
    @Binding var plainText: String
    let error: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Project story *").font(.headline)
            TiptapEditorView(documentJson: $documentJson, plainText: $plainText)
                .frame(minHeight: 340)
            if let error {
                Text(error).font(.caption).foregroundStyle(.red)
            }
        }
    }
}

private struct TiptapEditorView: UIViewRepresentable {
    @Binding var documentJson: String
    @Binding var plainText: String

    func makeCoordinator() -> Coordinator {
        Coordinator(documentJson: $documentJson, plainText: $plainText)
    }

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        configuration.userContentController.add(context.coordinator, name: "richText")
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.isOpaque = false
        webView.backgroundColor = .clear
        guard let url = Bundle.main.url(forResource: "index", withExtension: "html", subdirectory: "RichText") else {
            assertionFailure("RichText/index.html is missing from the application bundle.")
            return webView
        }
        webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        context.coordinator.documentJson = $documentJson
        context.coordinator.plainText = $plainText
    }

    static func dismantleUIView(_ webView: WKWebView, coordinator: Coordinator) {
        webView.configuration.userContentController.removeScriptMessageHandler(forName: "richText")
    }

    final class Coordinator: NSObject, WKScriptMessageHandler, WKNavigationDelegate {
        var documentJson: Binding<String>
        var plainText: Binding<String>

        init(documentJson: Binding<String>, plainText: Binding<String>) {
            self.documentJson = documentJson
            self.plainText = plainText
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            guard !documentJson.wrappedValue.isEmpty,
                  let data = documentJson.wrappedValue.data(using: .utf8),
                  let object = try? JSONSerialization.jsonObject(with: data),
                  let serialized = try? JSONSerialization.data(withJSONObject: object),
                  let literal = String(data: serialized, encoding: .utf8) else { return }
            webView.evaluateJavaScript("window.MobilingRichText.setContent(\(literal));")
        }

        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            guard let envelope = message.body as? [String: Any],
                  envelope["type"] as? String == "change",
                  let payload = envelope["payload"] as? [String: Any],
                  let document = payload["json"],
                  JSONSerialization.isValidJSONObject(document),
                  let data = try? JSONSerialization.data(withJSONObject: document),
                  let json = String(data: data, encoding: .utf8) else { return }
            DispatchQueue.main.async {
                self.documentJson.wrappedValue = json
                self.plainText.wrappedValue = payload["text"] as? String ?? ""
            }
        }
    }
}

private struct ProjectWizardStep: Hashable {
    let key: String
    let title: String
    let fields: [VendorNewField]
    let kinds: Set<String>
    let review: Bool

    init(_ key: String, _ title: String, _ fields: [VendorNewField], kinds: Set<String> = [], review: Bool = false) {
        self.key = key
        self.title = title
        self.fields = fields
        self.kinds = kinds
        self.review = review
    }
}

private let ProjectKindChoices: [(value: String, label: String)] = [
    ("charity_health_life", "Charity (health/life)"),
    ("social_non_profit", "Social (non-profit)"),
    ("goods_reputation_exchange", "Goods reputation exchange"),
    ("business_for_profit", "Business (for-profit)"),
]

private let ProjectWizardSteps = [
    ProjectWizardStep("base", "Basics", [VendorNewField("title", "Title", required: true)]),
    ProjectWizardStep("narrative", "Story", [VendorNewField("rawText", "Project story", required: true)]),
    ProjectWizardStep("business", "Business", [
        VendorNewField("offer", "Offer", required: true), VendorNewField("revenueModel", "Revenue model"), VendorNewField("traction", "Traction"),
    ], kinds: ["business_for_profit"]),
    ProjectWizardStep("exchange", "Exchange", [
        VendorNewField("itemSpec", "Goods offered", required: true), VendorNewField("handoffRules", "Handoff rules", required: true),
        VendorNewField("geography", "Area, city, or region"), VendorNewField("reputationAsk", "Expected reputation credit", required: true),
        VendorNewField("scoringPolicy", "Scoring policy"),
    ], kinds: ["goods_reputation_exchange"]),
    ProjectWizardStep("governance", "Governance", [
        VendorNewField("participationRule", "Participation rules"), VendorNewField("impactMetric", "Impact measurement"),
    ], kinds: ["social_non_profit"]),
    ProjectWizardStep("evidence", "Evidence", [
        VendorNewField("evidenceSummary", "Evidence summary"), VendorNewField("publicProofNote", "Public proof note"),
    ], kinds: ["charity_health_life", "goods_reputation_exchange"]),
    ProjectWizardStep("risk", "Risk", [VendorNewField("riskNote", "Risks and safeguards")], kinds: ["charity_health_life", "business_for_profit"]),
    ProjectWizardStep("review", "Review", [], review: true),
]

public struct ProjectNewWizardView: View {
    let gateway: VendorCrudGateway?
    let onRouteSelected: (String) -> Void

    @State private var values: [String: String] = ["kind": "social_non_profit"]
    @State private var currentStep = 0
    @State private var fieldErrors: [String: String] = [:]
    @State private var submitError: String?
    @State private var saving = false

    private var visibleSteps: [ProjectWizardStep] {
        let kind = values["kind", default: "social_non_profit"]
        return ProjectWizardSteps.filter { $0.kinds.isEmpty || $0.kinds.contains(kind) }
    }

    private var step: ProjectWizardStep {
        visibleSteps[min(currentStep, visibleSteps.count - 1)]
    }

    public var body: some View {
        Form {
            Section {
                ProgressView(value: Double(currentStep + 1), total: Double(visibleSteps.count))
                Text("Step \(currentStep + 1) of \(visibleSteps.count): \(step.title)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            if step.key == "base" {
                Section("Project kind") {
                    Picker("Project kind", selection: binding("kind")) {
                        ForEach(ProjectKindChoices, id: \.value) { choice in
                            Text(choice.label).tag(choice.value)
                        }
                    }
                    .pickerStyle(.navigationLink)
                }
            }
            if step.review {
                Section("Review") {
                    ForEach(values.keys.sorted(), id: \.self) { key in
                        if let value = values[key], !value.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(key).font(.caption).foregroundStyle(.secondary)
                                Text(value)
                            }
                        }
                    }
                }
            } else {
                Section(step.title) {
                    ForEach(step.fields, id: \.key) { field in
                        if field.key == "rawText" {
                            ProjectStoryRichTextEditor(
                                documentJson: binding("rawTextDocument"),
                                plainText: binding(field.key),
                                error: fieldErrors[field.key]
                            )
                        } else {
                            VStack(alignment: .leading, spacing: 4) {
                                TextField(field.label + (field.required ? " *" : ""), text: binding(field.key), axis: field.key == "title" || field.key == "geography" ? .horizontal : .vertical)
                                if let message = fieldErrors[field.key] {
                                    Text(message).font(.caption).foregroundStyle(.red)
                                }
                            }
                        }
                    }
                }
            }
            if let submitError {
                Section { Text(submitError).foregroundStyle(.red) }
            }
            Section {
                HStack {
                    Button(currentStep == 0 ? "Cancel" : "Back") {
                        if currentStep == 0 { onRouteSelected("vendor/project") } else { currentStep -= 1 }
                    }
                    Spacer()
                    if currentStep < visibleSteps.count - 1 {
                        Button("Next") { if validateStep() { currentStep += 1 } }
                    } else {
                        Button(saving ? "Creating…" : "Create Project") { submit() }
                            .disabled(saving)
                    }
                }
            }
        }
        .navigationTitle("New Project")
    }

    private func binding(_ key: String) -> Binding<String> {
        Binding(get: { values[key, default: ""] }, set: { values[key] = $0; fieldErrors[key] = nil })
    }

    private func validateStep() -> Bool {
        var errors: [String: String] = [:]
        for field in step.fields where field.required {
            if values[field.key, default: ""].trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                errors[field.key] = "\(field.label) is required."
            }
        }
        fieldErrors = errors
        return errors.isEmpty
    }

    private func submit() {
        Task {
            saving = true
            submitError = nil
            do {
                let payload = values.mapValues { $0.trimmingCharacters(in: .whitespacesAndNewlines) }.filter { !$0.value.isEmpty }
                try await gateway?.create(resource: "project", fields: payload)
                onRouteSelected("vendor/project")
            } catch {
                submitError = error.localizedDescription
            }
            saving = false
        }
    }
}

public let RetailNewFields = [
    VendorNewField("kind", "Listing type", required: true),
    VendorNewField("catalogCode", "Catalog", required: true),
    VendorNewField("categoryId", "Category", required: true),
    VendorNewField("title", "Title", required: true),
    VendorNewField("description", "Description"),
    VendorNewField("amountMinor", "Budget / price in cents", numeric: true),
    VendorNewField("currency", "Currency", required: true),
    VendorNewField("location", "Location"),
]
public let OrderNewFields = [
    VendorNewField("reference", "Reference", required: true), VendorNewField("status", "Status"), VendorNewField("total", "Total", numeric: true),
]
public let ProjectNewFields = [
    VendorNewField("title", "Title", required: true), VendorNewField("description", "Description"),
    VendorNewField("location", "Location"), VendorNewField("budget", "Budget", numeric: true),
