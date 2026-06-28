import SwiftUI

public struct MobileVendorTransactionView: View {
    private let vendorId: String?
    private let vendorTransactionGateway: VendorTransactionGateway?
    @State private var transaction: MobileVendorTransactionScreenContract?
    @State private var errorMessage: String?

    public init(vendorId: String?, vendorTransactionGateway: VendorTransactionGateway?) {
        self.vendorId = vendorId
        self.vendorTransactionGateway = vendorTransactionGateway
    }

    public var body: some View {
        List {
            Section { Text("Vendor Transaction").font(.headline) }
            if let errorMessage { Section { Text(errorMessage) } }
            else if let transaction, transaction.transactions.isEmpty { Section { Text("No vendor transactions yet.") } }
            else if let transaction {
                Section("Transaction") {
                    ForEach(transaction.transactions) { item in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.type ?? "Transaction").font(.headline)
                            Text((item.status ?? "—") + " · " + amountLabel(item.amount, item.currency))
                            Text((item.createdAt ?? "—") + " · " + (item.id ?? "—")).font(.caption).foregroundColor(.secondary)
                        }
                    }
                }
            } else { Section { Text("Loading vendor transaction...") } }
        }
        .navigationTitle("Vendor Transaction")
        .task(id: vendorId ?? "") { await load() }
    }

    private func load() async {
        transaction = nil; errorMessage = nil
        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { errorMessage = "Transaction requires an active vendor session."; return }
        guard let vendorTransactionGateway else { errorMessage = "Vendor transaction gateway is not available."; return }
        do { transaction = MobileVendorTransactionScreenContract(payload: try await LoadVendorTransactionUseCase(gateway: vendorTransactionGateway)(vendorId: vendorId)) }
        catch { errorMessage = error.localizedDescription }
    }

    private func amountLabel(_ amount: Double, _ currency: String?) -> String { let formatted = String(format: "%.2f", amount); guard let currency, !currency.isEmpty else { return formatted }; return currency + " " + formatted }
}
