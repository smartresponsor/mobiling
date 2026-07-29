import SwiftUI

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

public struct VendorNewCrudView: View {
    let singular: String
    let resource: String
    let listRoute: String
    let fields: [VendorNewField]
    let gateway: VendorCrudGateway?
    let onRouteSelected: (String) -> Void

    @State private var values: [String: String]
    @State private var fieldErrors: [String: String] = [:]
    @State private var submitError: String?
    @State private var saving = false

    public init(singular: String, resource: String, listRoute: String, fields: [VendorNewField], gateway: VendorCrudGateway?, onRouteSelected: @escaping (String) -> Void) {
        self.singular = singular
        self.resource = resource
        self.listRoute = listRoute
        self.fields = fields
        self.gateway = gateway
        self.onRouteSelected = onRouteSelected
        _values = State(initialValue: fields.reduce(into: [:]) { $0[$1.key] = "" })
    }

    public var body: some View {
        Form {
            Section {
                Text("Complete the required fields, then create the \(singular.lowercased()).")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            Section("Details") {
                ForEach(fields, id: \.key) { field in
                    VStack(alignment: .leading, spacing: 4) {
                        TextField(field.label + (field.required ? " *" : ""), text: binding(field.key), axis: field.key == "description" ? .vertical : .horizontal)
                            .keyboardType(field.numeric ? .decimalPad : .default)
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
    }

    private func binding(_ key: String) -> Binding<String> {
        Binding(
            get: { values[key, default: ""] },
            set: { values[key] = $0; fieldErrors[key] = nil }
        )
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
                        VStack(alignment: .leading, spacing: 4) {
                            TextField(field.label + (field.required ? " *" : ""), text: binding(field.key), axis: field.key == "title" || field.key == "geography" ? .horizontal : .vertical)
                            if let message = fieldErrors[field.key] {
                                Text(message).font(.caption).foregroundStyle(.red)
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

public let ProductNewFields = [
    VendorNewField("title", "Title", required: true), VendorNewField("description", "Description"),
    VendorNewField("price", "Price", numeric: true), VendorNewField("status", "Status"),
]
public let OrderNewFields = [
    VendorNewField("reference", "Reference", required: true), VendorNewField("status", "Status"), VendorNewField("total", "Total", numeric: true),
]
public let ProjectNewFields = [
    VendorNewField("title", "Title", required: true), VendorNewField("description", "Description"),
    VendorNewField("location", "Location"), VendorNewField("budget", "Budget", numeric: true),
