import SwiftUI

public struct WalletOverviewView: View {
    let gateway: WalletGateway?
    @State private var balance: WalletBalancePayload?
    @State private var error: String?

    public init(gateway: WalletGateway?) { self.gateway = gateway }

    public var body: some View {
        List {
            Section("Balance") {
                if let item = balance?.currency.first {
                    walletMetric("Available", item.availableMinor, item.code)
                    walletMetric("Reserved", item.reservedMinor, item.code)
                    walletMetric("Total", item.totalMinor, item.code)
                } else {
                    Text(error ?? "Wallet is ready to activate when your first balance is created.").foregroundStyle(.secondary)
                }
            }
            Section("Activity") {
                NavigationLink("Transactions") { WalletTransactionView(gateway: gateway) }
                NavigationLink("Funding") { WalletFundingView(gateway: gateway) }
                NavigationLink("Withdrawals") { WalletWithdrawalListView(gateway: gateway) }
            }
        }
        .navigationTitle("Wallet")
        .task { await reload() }
        .refreshable { await reload() }
    }

    @MainActor private func reload() async {
        guard let gateway else { return }
        do { balance = try await gateway.loadBalance(); error = nil }
        catch { error = error.localizedDescription }
    }
}

public struct WalletTransactionView: View {
    let gateway: WalletGateway?
    @State private var items: [WalletTransactionItem] = []
    @State private var error: String?

    public var body: some View {
        List {
            if items.isEmpty { Text(error ?? "No wallet transactions yet.").foregroundStyle(.secondary) }
            ForEach(items) { item in
                VStack(alignment: .leading, spacing: 4) {
                    HStack { Text(item.type.capitalized).font(.headline); Spacer(); Text(walletMoney(item.amountMinor, item.currency)).font(.headline) }
                    Text(item.postedAt).font(.caption).foregroundStyle(.secondary)
                }
            }
        }
        .navigationTitle("Transactions")
        .task { await reload() }
        .refreshable { await reload() }
    }

    @MainActor private func reload() async {
        guard let gateway else { return }
        do { items = try await gateway.loadTransactions(); error = nil }
        catch { error = error.localizedDescription }
    }
}

public struct WalletFundingView: View {
    let gateway: WalletGateway?
    @State private var items: [WalletOperationItem] = []
    @State private var error: String?

    public var body: some View {
        List {
            if items.isEmpty { Text(error ?? "No funding activity yet.").foregroundStyle(.secondary) }
            ForEach(items) { item in
                HStack { VStack(alignment: .leading) { Text(item.status.capitalized).font(.headline); Text(walletShort(item.id)).font(.caption).foregroundStyle(.secondary) }; Spacer(); Text(walletMoney(item.amountMinor, item.currency)).font(.headline) }
            }
        }
        .navigationTitle("Funding")
        .task { await reload() }
        .refreshable { await reload() }
    }

    @MainActor private func reload() async {
        guard let gateway else { return }
        do { items = try await gateway.loadFunding(); error = nil }
        catch { error = error.localizedDescription }
    }
}

public struct WalletWithdrawalListView: View {
    let gateway: WalletGateway?
    @State private var items: [WalletOperationItem] = []
    @State private var destinations: [WalletWithdrawalDestination] = []
    @State private var selectedDestinationId = ""
    @State private var amount = ""
    @State private var message: String?
    @State private var busy = false

    public var body: some View {
        List {
            Section("New withdrawal") {
                if destinations.isEmpty {
                    Text("No active withdrawal destination is available.").foregroundStyle(.secondary)
                } else {
                    Picker("Destination", selection: $selectedDestinationId) {
                        ForEach(destinations) { destination in Text(destination.label).tag(destination.id) }
                    }
                    TextField("Amount (USD)", text: $amount).keyboardType(.decimalPad)
                    Button(busy ? "Working…" : "Request withdrawal") { Task { await requestWithdrawal() } }
                        .disabled(busy || selectedDestinationId.isEmpty || walletAmountMinor(amount) == nil)
                }
                if let message { Text(message).font(.footnote).foregroundStyle(.secondary) }
            }
            Section("History") {
                if items.isEmpty { Text("No withdrawals yet.").foregroundStyle(.secondary) }
                ForEach(items) { item in
                    NavigationLink {
                        WalletWithdrawalDetailView(withdrawalId: item.id, gateway: gateway)
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item.status.capitalized).font(.headline)
                                if let destination = item.destinationReference { Text("Destination: \(walletDisplayReference(destination))").font(.caption).foregroundStyle(.secondary) }
                                Text(walletShort(item.id)).font(.caption2).foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text(walletMoney(item.amountMinor, item.currency)).font(.headline)
                        }
                    }
                }
            }
        }
        .navigationTitle("Withdrawals")
        .task { await reload() }
        .refreshable { await reload() }
    }

    @MainActor private func reload() async {
        guard let gateway else { return }
        do {
            async let withdrawals = gateway.loadWithdrawals()
            async let destinationList = gateway.loadWithdrawalDestinations()
            items = try await withdrawals
            destinations = try await destinationList
            if !destinations.contains(where: { $0.id == selectedDestinationId }) { selectedDestinationId = destinations.first?.id ?? "" }
        } catch { message = error.localizedDescription }
    }

    @MainActor private func requestWithdrawal() async {
        guard let gateway, let amountMinor = walletAmountMinor(amount), !selectedDestinationId.isEmpty else { return }
        busy = true; defer { busy = false }
        do {
            _ = try await gateway.requestWithdrawal(amountMinor: amountMinor, currency: "USD", paymentInstrumentId: selectedDestinationId, idempotencyKey: UUID().uuidString.lowercased())
            amount = ""
            message = "Withdrawal reserved. External payout processing is not enabled yet."
            await reload()
        } catch { message = error.localizedDescription }
    }
}

public struct WalletWithdrawalDetailView: View {
    let withdrawalId: String
    let gateway: WalletGateway?
    @State private var item: WalletOperationItem?
    @State private var error: String?
    @State private var busy = false

    public var body: some View {
        List {
            if let item {
                Section("Withdrawal") {
                    HStack { Text(item.status.capitalized).font(.headline); Spacer(); Text(walletMoney(item.amountMinor, item.currency)).font(.headline) }
                    if let destination = item.destinationReference { walletLabeledRow("Destination", walletDisplayReference(destination)) }
                    if let reservation = item.sourceReference { walletLabeledRow("Reservation", walletShort(reservation)) }
                    if let rail = item.railReference { walletLabeledRow("Rail", walletShort(rail)) }
                    walletLabeledRow("ID", walletShort(item.id))
                }
                Section("Lifecycle") {
                    ForEach(walletTimeline(item.status), id: \.title) { step in
                        HStack(alignment: .top, spacing: 10) {
                            Image(systemName: step.complete ? "checkmark.circle.fill" : "circle").foregroundStyle(step.complete ? Color.accentColor : Color.secondary)
                            VStack(alignment: .leading, spacing: 2) { Text(step.title).fontWeight(step.current ? .semibold : .regular); Text(step.description).font(.footnote).foregroundStyle(.secondary) }
                        }
                    }
                }
                if item.status == "reserved" {
                    Section { Button(busy ? "Cancelling…" : "Cancel withdrawal", role: .destructive) { Task { await cancel() } }.disabled(busy) }
                }
            } else {
                Text(error ?? "Loading withdrawal…").foregroundStyle(.secondary)
            }
        }
        .navigationTitle("Withdrawal detail")
        .task { await reload() }
    }

    @MainActor private func reload() async {
        guard let gateway else { return }
        do { item = try await gateway.loadWithdrawal(id: withdrawalId); error = nil }
        catch { error = error.localizedDescription }
    }

    @MainActor private func cancel() async {
        guard let gateway, let item else { return }
        busy = true; defer { busy = false }
        do { _ = try await gateway.cancelWithdrawal(id: item.id); await reload() }
        catch { error = error.localizedDescription }
    }
}

private struct WalletTimelineStep { let title: String; let description: String; let complete: Bool; let current: Bool }

private func walletTimeline(_ status: String) -> [WalletTimelineStep] {
    let order = ["pending", "reserved", "processing", "succeeded"]
    let terminal = ["failed", "cancelled", "reversed"].contains(status)
    let reached = order.firstIndex(of: status) ?? (terminal ? 1 : 0)
    var steps = [
        WalletTimelineStep(title: "Requested", description: "Withdrawal request created.", complete: true, current: status == "pending"),
        WalletTimelineStep(title: "Funds reserved", description: "Wallet funds are held for cash-out.", complete: reached >= 1 || terminal, current: status == "reserved")
    ]
    if status != "cancelled" { steps.append(WalletTimelineStep(title: "Processing", description: "External payout rail accepts the withdrawal.", complete: reached >= 2 || ["succeeded", "reversed", "failed"].contains(status), current: status == "processing")) }
    if ["succeeded", "reversed"].contains(status) { steps.append(WalletTimelineStep(title: "Completed", description: "Funds were finalized from the wallet.", complete: true, current: status == "succeeded")) }
    if terminal {
        let description = status == "cancelled" ? "Reserved wallet funds were released." : status == "failed" ? "Processing stopped and reserved funds were released or remain recoverable." : "A completed withdrawal was reversed."
        steps.append(WalletTimelineStep(title: status.capitalized, description: description, complete: true, current: true))
    }
    return steps
}

@ViewBuilder private func walletLabeledRow(_ title: String, _ value: String) -> some View {
    HStack { Text(title); Spacer(); Text(value).foregroundStyle(.secondary) }
}

@ViewBuilder private func walletMetric(_ title: String, _ amountMinor: Int64, _ currency: String) -> some View {
    HStack { Text(title); Spacer(); Text(walletMoney(amountMinor, currency)).fontWeight(.semibold) }
}

private func walletMoney(_ amountMinor: Int64, _ currency: String) -> String { "\(currency) \(String(format: "%.2f", Double(amountMinor) / 100.0))" }
private func walletShort(_ value: String) -> String { value.count <= 14 ? value : "\(value.prefix(8))…\(value.suffix(4))" }
private func walletDisplayReference(_ value: String) -> String { (value.split(separator: ":", maxSplits: 1).last.map(String.init) ?? value).replacingOccurrences(of: "-", with: " ") }
private func walletAmountMinor(_ value: String) -> Int64? {
    let parts = value.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: ".", omittingEmptySubsequences: false)
    guard parts.count <= 2, !parts[0].isEmpty, parts[0].allSatisfy({ $0.isNumber }) else { return nil }
    let fractional = parts.count == 2 ? String(parts[1]) : ""
    guard fractional.count <= 2, fractional.allSatisfy({ $0.isNumber }) else { return nil }
    guard let whole = Int64(parts[0]) else { return nil }
    let cents = Int64(fractional.padding(toLength: 2, withPad: "0", startingAt: 0)) ?? 0
    let total = whole * 100 + cents
    return total > 0 ? total : nil
}