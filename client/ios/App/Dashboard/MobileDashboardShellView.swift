import SwiftUI

public struct MobileDashboardShellView: View {
    private let navigationShellGateway: NavigationShellGateway?
    private let vendorId: String?
    private let vendorProfileGateway: VendorProfileGateway?
    private let vendorSummaryGateway: VendorSummaryGateway?
    private let vendorStatementGateway: VendorStatementGateway?
    private let onSignOut: () -> Void

    @State private var selectedRoute: String = "dashboard"
    @State private var vendorContentRoute: String = "vendor"
    @State private var accountOpen: Bool = false
    @State private var shell: MobileNavigationShellScreenContract = MobileDashboardShellView.fallbackShell()

    public init(navigationShellGateway: NavigationShellGateway? = nil, vendorId: String? = nil, vendorProfileGateway: VendorProfileGateway? = nil, vendorSummaryGateway: VendorSummaryGateway? = nil, vendorStatementGateway: VendorStatementGateway? = nil, onSignOut: @escaping () -> Void) {
        self.navigationShellGateway = navigationShellGateway
        self.vendorId = vendorId
        self.vendorProfileGateway = vendorProfileGateway
        self.vendorSummaryGateway = vendorSummaryGateway
        self.vendorStatementGateway = vendorStatementGateway
        self.onSignOut = onSignOut
    }

    public var body: some View {
        TabView(selection: $selectedRoute) {
            NavigationView {
                content(title: "Dashboard", items: shell.bottomPrimary)
                    .toolbar { accountToolbar }
            }
            .tabItem { Label("Dashboard", systemImage: "house") }
            .tag("dashboard")

            NavigationView {
                if vendorContentRoute == "vendor/profile" {
                    MobileVendorProfileView(vendorId: vendorId, vendorProfileGateway: vendorProfileGateway)
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/summary" {
                    MobileVendorSummaryView(vendorId: vendorId, vendorSummaryGateway: vendorSummaryGateway)
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/statement" {
                    MobileVendorStatementView(vendorId: vendorId, vendorStatementGateway: vendorStatementGateway)
                        .toolbar { accountToolbar }
                } else {
                    content(title: "Vendor", items: vendorContentRoute == "vendor" ? shell.vendorContext : [])
                        .toolbar { accountToolbar }
                }
            }
            .tabItem { Label("Vendor", systemImage: "storefront") }
            .tag("vendor")

            NavigationView {
                content(title: "More", items: shell.moreDrawer)
                    .toolbar { accountToolbar }
            }
            .tabItem { Label("More", systemImage: "line.3.horizontal") }
            .tag("more")
        }
        .sheet(isPresented: $accountOpen) {
            NavigationView {
                List {
                    Section("Account") {
                        ForEach(shell.accountQuick.filter { $0.visible }) { item in
                            row(item)
                                .onTapGesture { handle(item) }
                        }
                    }
                }
                .navigationTitle("Account")
                .navigationBarTitleDisplayMode(.inline)
            }
            
        }
        .task {
            guard let navigationShellGateway else {
                return
            }

            do {
                shell = MobileNavigationShellScreenContract(
                    payload: try await LoadNavigationShellUseCase(gateway: navigationShellGateway)()
                )
            } catch {
                shell = MobileDashboardShellView.fallbackShell()
            }
        }
    }

    @ToolbarContentBuilder
    private var accountToolbar: some ToolbarContent {
        ToolbarItem(placement: .navigationBarTrailing) {
            Button("Account") { accountOpen = true }
        }
    }

    private func content(title: String, items: [MobileNavigationItemPayload]) -> some View {
        List {
            Section {
                Text("Root shell is loaded from Navigating publication. Inactive modules stay visible as Coming soon.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

            Section(title) {
                ForEach(items.filter { $0.visible }) { item in
                    row(item)
                        .onTapGesture { handle(item) }
                }
            }
        }
        .navigationTitle(title)
    }

    private func row(_ item: MobileNavigationItemPayload) -> some View {
        HStack(spacing: 12) {
            Image(systemName: systemImage(for: item))
                .foregroundColor(item.enabled ? .blue : .secondary)
                .frame(width: 24)

            VStack(alignment: .leading, spacing: 2) {
                Text(item.label)
                Text(item.badge ?? item.route ?? item.key)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            if !item.enabled {
                Text("Coming soon")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(.quaternary, in: Capsule())
            }
        }
        .opacity(item.enabled ? 1.0 : 0.55)
    }

    private func handle(_ item: MobileNavigationItemPayload) {
        if item.action == "access.sign_out" {
            onSignOut()
            return
        }

        guard item.enabled, let route = item.route else {
            return
        }

        guard isHandledRoute(route) else {
            return
        }

        if route.hasPrefix("vendor/") {
            vendorContentRoute = route
            selectedRoute = "vendor"
            accountOpen = false
            return
        }

        if route == "vendor" {
            vendorContentRoute = "vendor"
        }

        selectedRoute = route
        accountOpen = false
    }

    private func systemImage(for item: MobileNavigationItemPayload) -> String {
        switch item.icon {
        case "store": return "storefront"
        case "person": return "person.crop.circle"
        case "attachment": return "paperclip"
        case "message": return "message"
        case "catalog": return "bag"
        case "key": return "key"
        case "logout": return "rectangle.portrait.and.arrow.right"
        case "menu": return "line.3.horizontal"
        case "summary": return "chart.bar"
        case "statement": return "doc.text"
        case "payout": return "dollarsign.circle"
        case "receipt": return "list.bullet.rectangle"
        default: return "house"
        }
    }

    private func isHandledRoute(_ route: String) -> Bool {
        ["dashboard", "vendor", "vendor/profile", "vendor/summary", "vendor/statement", "vendor/payout", "vendor/transaction", "more"].contains(route)
    }

    private static func fallbackShell() -> MobileNavigationShellScreenContract {
        MobileNavigationShellScreenContract(
            bottomPrimary: [
                item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
                item("vendor", "Vendor", "store", true, "vendor"),
                item("more", "More", "menu", true, "more"),
            ],
            accountQuick: [
                item("vendor_profile", "My Profile", "person", true, "vendor/profile"),
                item("access_password", "Change Password", "key", false, "access/password"),
                item("access_verification", "Verification", "key", false, "access/verification"),
                item("vendor_attachment", "My Attachments", "attachment", false, "attachment"),
                item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action: "access.sign_out"),
            ],
            moreDrawer: [
                item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
                item("vendor", "Vendor", "store", true, "vendor"),
                item("catalog", "Catalog", "catalog", false, "catalog"),
                item("message", "Message", "message", false, "message"),
                item("attachment", "Attachments", "attachment", false, "attachment"),
            ],
            vendorContext: [
                item("vendor_overview", "My Vendor", "store", true, "vendor"),
                item("vendor_profile", "My Profile", "person", true, "vendor/profile"),
                item("vendor_summary", "Summary", "summary", true, "vendor/summary"),
                item("vendor_statement", "Statement", "statement", true, "vendor/statement"),
                item("vendor_payout", "Payout", "payout", true, "vendor/payout"),
                item("vendor_transaction", "Transaction", "receipt", true, "vendor/transaction"),
                item("vendor_attachment", "My Attachments", "attachment", false, "attachment"),
            ]
        )
    }

    private static func item(_ key: String, _ label: String, _ icon: String, _ enabled: Bool, _ route: String, action: String? = nil) -> MobileNavigationItemPayload {
        MobileNavigationItemPayload(
            id: key,
            key: key,
            label: label,
            icon: icon,
            badge: enabled ? nil : "Coming soon",
            enabled: enabled,
            visible: true,
            status: enabled ? "active" : "coming_soon",
            disabledReason: enabled ? nil : "component_disabled",
            requiredComponent: nil,
            location: "mobile",
            group: "fallback",
            groupLabel: "Fallback",
            action: action,
            route: route
        )
    }
}



