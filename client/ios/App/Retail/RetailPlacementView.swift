import SwiftUI

public struct RetailPlacementView: View {
    let retailId: String
    let gateway: RetailPlacementGateway?
    let onRouteSelected: (String) -> Void

    @State private var snapshot: RetailPlacementSnapshot?
    @State private var values: [String: String] = [:]
    @State private var error: String?
    @State private var saving = false

    public var body: some View {
        Form {
            Section("Listing") {
                Text(snapshot?.title ?? "Listing placement")
                    .font(.headline)
                Text("Listing #\(retailId)")
                    .foregroundStyle(.secondary)
                if let status = snapshot?.status {
                    Text("Status: \(status)")
                        .foregroundStyle(.secondary)
                }
            }

            if let snapshot {
                placementSection(snapshot)
                if let error {
                    Section { Text(error).foregroundStyle(.red) }
                }
                if snapshot.nextStep != "complete" {
                    Section {
                        Button(snapshot.nextStep == "review" ? "Publish" : "Continue") {
                            submit(snapshot)
                        }
                        .disabled(saving)
                        Button("Exit", role: .cancel) {
                            onRouteSelected("vendor/retail")
                        }
                        .disabled(saving)
                    }
                }
            } else {
                Section {
                    if let error {
                        Text(error).foregroundStyle(.red)
                    } else {
                        ProgressView("Loading placement…")
                    }
                }
            }
        }
        .navigationTitle("Placement")
        .task(id: retailId) { await load() }
    }

    @ViewBuilder
    private func placementSection(_ snapshot: RetailPlacementSnapshot) -> some View {
        switch snapshot.nextStep {
        case "fulfillment":
            Section("Fulfillment") {
                Picker("Fulfillment", selection: binding("mode")) {
                    Text("Choose fulfillment").tag("")
                    ForEach(fulfillmentModes(snapshot.kind), id: \.self) { mode in
                        Text(choiceLabel(mode)).tag(mode)
                    }
                }
                .pickerStyle(.menu)
                TextField("Service area / region", text: binding("serviceArea"))
                TextField("Radius (km)", text: binding("radiusKm"))
                    .keyboardType(.decimalPad)
                if snapshot.kind == "goods", values["mode"] == "shipping" {
                    TextField("Weight (kg)", text: binding("weightKg"))
                        .keyboardType(.decimalPad)
                    Picker("Priority", selection: binding("priority")) {
                        Text("Choose priority").tag("")
                        ForEach(["STANDARD", "EXPRESS", "OVERNIGHT"], id: \.self) { priority in
                            Text(choiceLabel(priority)).tag(priority)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

        case "location":
            Section("Location") {
                TextField("Address", text: binding("line1"))
                TextField("Address line 2", text: binding("line2"))
                TextField("City", text: binding("city"))
                TextField("State / region", text: binding("region"))
                TextField("Postal code", text: binding("postalCode"))
                TextField("Country code", text: binding("countryCode"))
                    .textInputAutocapitalization(.characters)
            }

        case "pricing":
            Section("Pricing") {
                Picker("Pricing", selection: binding("model")) {
                    Text("Choose pricing").tag("")
                    ForEach(pricingModels(snapshot.kind), id: \.self) { model in
                        Text(choiceLabel(model)).tag(model)
                    }
                }
                .pickerStyle(.menu)
                if let model = values["model"], !model.isEmpty, model != "quote" {
                    TextField("Amount (minor units)", text: binding("amountMinor"))
                        .keyboardType(.numberPad)
                }
                if values["model"] == "range" {
                    TextField("Maximum amount", text: binding("maximumAmountMinor"))
                        .keyboardType(.numberPad)
                }
                TextField("Currency", text: binding("currency", defaultValue: "USD"))
                    .textInputAutocapitalization(.characters)
            }

        case "review":
            Section("Review") {
                Text("Fulfillment: \(snapshot.fulfillmentProfile ?? "Configured")")
                Text("Location: \(snapshot.locationProfile ?? "Not required")")
                Text("Pricing: \(snapshot.pricingProfile ?? "Configured")")
            }

        case "complete":
            Section("Complete") {
                Text("The listing is published.")
                Button("Back to listings") {
                    onRouteSelected("vendor/retail")
                }
            }

        default:
            Section {
                Text("Unknown placement step: \(snapshot.nextStep)")
                    .foregroundStyle(.red)
            }
        }
    }

    private func choiceLabel(_ value: String) -> String {
        value.lowercased().replacingOccurrences(of: "_", with: " ").capitalized
    }

    private func binding(_ key: String, defaultValue: String = "") -> Binding<String> {
        Binding(
            get: { values[key] ?? defaultValue },
            set: { values[key] = $0 }
        )
    }

    @MainActor
    private func load() async {
        do {
            guard let gateway else {
                throw NSError(domain: "RetailPlacement", code: 500, userInfo: [NSLocalizedDescriptionKey: "Retail placement gateway is not available."])
            }
            snapshot = try await gateway.snapshot(retailId: retailId)
            values = [:]
            error = nil
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func submit(_ current: RetailPlacementSnapshot) {
        Task { @MainActor in
            saving = true
            error = nil
            do {
                guard let gateway else {
                    throw NSError(domain: "RetailPlacement", code: 500, userInfo: [NSLocalizedDescriptionKey: "Retail placement gateway is not available."])
                }
                let payload = values
                    .mapValues { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                    .filter { !$0.value.isEmpty }
                snapshot = switch current.nextStep {
                case "fulfillment": try await gateway.configureFulfillment(retailId: retailId, fields: payload)
                case "location": try await gateway.configureLocation(retailId: retailId, fields: payload)
                case "pricing": try await gateway.configurePricing(retailId: retailId, fields: payload)
                case "review": try await gateway.publish(retailId: retailId)
                default: current
                }
                values = [:]
            } catch {
                self.error = error.localizedDescription
            }
            saving = false
        }
    }

    private func fulfillmentModes(_ kind: String) -> [String] {
        kind == "goods" ? ["shipping", "pickup", "digital"] : ["onsite", "remote", "hybrid"]
    }

    private func pricingModels(_ kind: String) -> [String] {
        switch kind {
        case "service": return ["fixed", "hourly", "minimum", "quote"]
        case "goods": return ["fixed", "deposit"]
