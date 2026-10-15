import SwiftUI

public struct CartMobileScreen: View {
    @Environment(\.openURL) private var openURL
    private let bridge: CartFeatureBridge?

    @State private var cart: CartMobilePayload?
    @State private var loading = true
    @State private var preparingCheckout = false
    @State private var errorMessage: String?
    @State private var checkoutStatus: String?

    public init(cartFeatureBridge: CartFeatureBridge?) {
        self.bridge = cartFeatureBridge
    }

    public var body: some View {
        List {
            if loading {
                ProgressView("Loading cart…")
            } else if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.secondary)
            } else if let cart {
                Section("Cart") {
                    Text("Status: \(cart.status)")
                    Text("\(cart.itemCount) items")
                }

                if cart.items.isEmpty {
                    Text("Your cart is empty.")
                        .foregroundStyle(.secondary)
                } else {
                    Section("Items") {
                        ForEach(cart.items) { item in
                            HStack(alignment: .top, spacing: MobileDesignDefaults.Spacing.md) {
                                VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                                    Text(item.title)
                                        .font(.headline)
                                    Text("\(item.quantity) × \(money(item.unitPriceMinor, item.currencyCode))")
                                        .font(.footnote)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                Text(money(item.lineTotalMinor, item.currencyCode))
                                    .fontWeight(.semibold)
                            }
                        }
                    }
                }

                Section("Total") {
                    HStack {
                        Text("Total")
                        Spacer()
                        Text(money(cart.totalMinor, cart.currencyCode))
                            .fontWeight(.semibold)
                    }
                }

                Section {
                    Button {
                        Task { await prepareCheckout() }
                    } label: {
                        if preparingCheckout {
                            ProgressView()
                        } else {
                            Text("Prepare checkout")
                        }
                    }
                    .disabled(cart.items.isEmpty || preparingCheckout)
                    if let checkoutStatus {
                        Text(checkoutStatus)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
        .navigationTitle("Cart")
        .refreshable { await load() }
        .task { await load() }
    }

    @MainActor private func load() async {
        guard let bridge else {
            loading = false
            errorMessage = "Cart service is not available."
            return
        }

        loading = true
        do {
            cart = try await bridge.current()
            errorMessage = nil
        } catch {
            cart = nil
            errorMessage = error.localizedDescription
        }
        loading = false
    }

    @MainActor private func prepareCheckout() async {
        guard let bridge, !preparingCheckout else { return }
        preparingCheckout = true
        checkoutStatus = nil
        defer { preparingCheckout = false }
        do {
            let handoff = try await bridge.checkoutHandoff()
            if let checkoutUrl = handoff.checkoutUrl, let url = URL(string: checkoutUrl) {
                checkoutStatus = "Opening checkout…"
                openURL(url)
            } else {
                checkoutStatus = handoff.status == "prepared" ? "Checkout is ready." : "Checkout status: \(handoff.status)"
            }
        } catch {
            checkoutStatus = error.localizedDescription
        }
    }

    private func money(_ amountMinor: Int64, _ currencyCode: String) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = currencyCode
        formatter.locale = .current
        let major = NSDecimalNumber(value: amountMinor).dividing(by: 100)
        return formatter.string(from: major) ?? "\(currencyCode) \(major.stringValue)"
    }
}
