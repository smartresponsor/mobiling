import SwiftUI

public struct MobileVendorPayoutView: View {
    private let vendorId: String?
    private let vendorPayoutGateway: VendorPayoutGateway?
    @State private var payout: MobileVendorPayoutScreenContract?
    @State private var errorMessage: String?

    public init(vendorId: String?, vendorPayoutGateway: VendorPayoutGateway?) {
        self.vendorId = vendorId
        self.vendorPayoutGateway = vendorPayoutGateway
    }

    public var body: some View {
        List {
            Section { Text("Vendor Payout").font(.headline) }
            if let errorMessage { Section { Text(errorMessage) } }
            else if let payout {
                Section("Payout") {
                    Text("Vendor ID: \(payout.vendorId)")
                    Text("Status: \(payout.payoutStatus ?? "—")")
                    Text("Account: \(payout.payoutAccountLabel ?? "—")")
                    Text("Available: \(amountLabel(payout.availableAmount, payout.currency))")
                    Text("Pending: \(amountLabel(payout.pendingAmount, payout.currency))")
                }
            } else { Section { Text("Loading vendor payout...") } }
        }
        .navigationTitle("Vendor Payout")
        .task(id: vendorId ?? "") { await load() }
    }

    private func load() async {
        payout = nil; errorMessage = nil
        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { errorMessage = "Payout requires an active vendor session."; return }
        guard let vendorPayoutGateway else { errorMessage = "Vendor payout gateway is not available."; return }
        do { payout = MobileVendorPayoutScreenContract(payload: try await LoadVendorPayoutUseCase(gateway: vendorPayoutGateway)(vendorId: vendorId)) }
        catch { errorMessage = error.localizedDescription }
    }

    private func amountLabel(_ amount: Double, _ currency: String?) -> String { let formatted = String(format: "%.2f", amount); guard let currency, !currency.isEmpty else { return formatted }; return currency + " " + formatted }
}
