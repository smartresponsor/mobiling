import SwiftUI

public struct MobileVendorSummaryView: View {
    private let vendorId: String?
    private let vendorSummaryGateway: VendorSummaryGateway?

    @State private var summary: MobileVendorSummaryScreenContract?
    @State private var errorMessage: String?

    public init(vendorId: String?, vendorSummaryGateway: VendorSummaryGateway?) {
        self.vendorId = vendorId
        self.vendorSummaryGateway = vendorSummaryGateway
    }

    public var body: some View {
        List {
            Section { Text("Vendor Summary").font(.headline) }

            if let errorMessage {
                Section { Text(errorMessage) }
            } else if let summary {
                Section("Identity") {
                    field("Vendor ID", summary.vendorId)
                    field("Brand", summary.brandName)
                    field("Status", summary.status)
                }

                Section("Readiness") {
                    ProgressView(value: Double(summary.profileCompletionPercent), total: 100)
                    Text("Profile completion: \(summary.profileCompletionPercent)%")
                    field("Next action", summary.nextAction)
                }
            } else {
                Section { Text("Loading vendor summary...") }
            }
        }
        .navigationTitle("Vendor Summary")
        .task(id: vendorId ?? "") { await load() }
    }

    @ViewBuilder
    private func field(_ label: String, _ value: String?) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption).foregroundColor(.secondary)
            Text(value?.isEmpty == false ? value! : "—")
        }
    }

    private func load() async {
        summary = nil
        errorMessage = nil

        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Summary requires an active vendor session."
            return
        }

        guard let vendorSummaryGateway else {
            errorMessage = "Vendor summary gateway is not available."
            return
        }

        do {
            let payload = try await LoadVendorSummaryUseCase(gateway: vendorSummaryGateway)(vendorId: vendorId)
            summary = MobileVendorSummaryScreenContract(payload: payload)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
