import Foundation
import SwiftUI

public struct MobileVendorStatementView: View {
    private let vendorId: String?
    private let vendorStatementGateway: VendorStatementGateway?

    @State private var statement: MobileVendorStatementScreenContract?
    @State private var errorMessage: String?

    public init(vendorId: String?, vendorStatementGateway: VendorStatementGateway?) {
        self.vendorId = vendorId
        self.vendorStatementGateway = vendorStatementGateway
    }

    public var body: some View {
        List {
            Section { Text("Vendor Statement").font(.headline) }

            if let errorMessage {
                Section { Text(errorMessage) }
            } else if let statement {
                Section("Statement") {
                    field("Vendor ID", statement.vendorId)
                    field("Status", statement.statementStatus)
                    field("Currency", statement.currency)
                    field("Gross amount", amountLabel(statement.grossAmount, statement.currency))
                    field("Net amount", amountLabel(statement.netAmount, statement.currency))
                }
            } else {
                Section { Text("Loading vendor statement...") }
            }
        }
        .navigationTitle("Vendor Statement")
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
        statement = nil
        errorMessage = nil

        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Statement requires an active vendor session."
            return
        }

        guard let vendorStatementGateway else {
            errorMessage = "Vendor statement gateway is not available."
            return
        }

        do {
            let payload = try await LoadVendorStatementUseCase(gateway: vendorStatementGateway)(vendorId: vendorId)
            statement = MobileVendorStatementScreenContract(payload: payload)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func amountLabel(_ amount: Double, _ currency: String?) -> String {
        let formatted = String(format: "%.2f", amount)
        guard let currency, !currency.isEmpty else { return formatted }
        return currency + " " + formatted
    }
}
