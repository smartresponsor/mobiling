import SwiftUI

public struct MobileDashboardShellView: View {
    private let navigationShellGateway: NavigationShellGateway?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?
    private let catalogFeatureBridge: CatalogFeatureBridge?
    private let vendorId: String?
    private let vendorProfileGateway: VendorProfileGateway?
    private let vendorSummaryGateway: VendorSummaryGateway?
    private let vendorStatementGateway: VendorStatementGateway?
    private let vendorPayoutGateway: VendorPayoutGateway?
    private let vendorTransactionGateway: VendorTransactionGateway?
    private let vendorCrudGateway: VendorCrudGateway?
    private let initialRoute: String
    private let catalogEnabled: Bool
    private let navigationLabelResolver: (String?, String, String) -> String
    private let onSignOut: () -> Void

    @State private var selectedRoute: String
    @State private var vendorContentRoute: String = "vendor"
    @State private var navigationOpen: Bool = false
    @State private var accountOpen: Bool = false
    @State private var newChooserOpen: Bool = false
    @State private var shell: MobileNavigationShellScreenContract = MobileDashboardShellView.fallbackShell()

    public init(navigationShellGateway: NavigationShellGateway? = nil, attachmentFeatureBridge: AttachmentFeatureBridge? = nil, catalogFeatureBridge: CatalogFeatureBridge? = nil, vendorId: String? = nil, vendorProfileGateway: VendorProfileGateway? = nil, vendorSummaryGateway: VendorSummaryGateway? = nil, vendorStatementGateway: VendorStatementGateway? = nil, vendorPayoutGateway: VendorPayoutGateway? = nil, vendorTransactionGateway: VendorTransactionGateway? = nil, vendorCrudGateway: VendorCrudGateway? = nil, initialRoute: String = "dashboard", catalogEnabled: Bool = true, navigationLabelResolver: @escaping (String?, String, String) -> String = { _, _, label in label }, onSignOut: @escaping () -> Void) {
        self.navigationShellGateway = navigationShellGateway
        self.attachmentFeatureBridge = attachmentFeatureBridge
        self.catalogFeatureBridge = catalogFeatureBridge
        self.vendorId = vendorId
        self.vendorProfileGateway = vendorProfileGateway
        self.vendorSummaryGateway = vendorSummaryGateway
        self.vendorStatementGateway = vendorStatementGateway
        self.vendorPayoutGateway = vendorPayoutGateway
        self.vendorTransactionGateway = vendorTransactionGateway
        self.vendorCrudGateway = vendorCrudGateway
        self.initialRoute = initialRoute
        self.catalogEnabled = catalogEnabled
        self.navigationLabelResolver = navigationLabelResolver
        self._selectedRoute = State(initialValue: initialRoute)
        self.onSignOut = onSignOut
    }

    public var body: some View {
        TabView(selection: $selectedRoute) {
            NavigationView {
                content(title: "Dashboard", items: shell.bottomPrimary)
                    .toolbar { accountToolbar }
            }
            .tabItem { Label(navigationLabelResolver("dashboard", "dashboard", "Dashboard"), systemImage: "house") }
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
                } else if vendorContentRoute == "vendor/payout" {
                    MobileVendorPayoutView(vendorId: vendorId, vendorPayoutGateway: vendorPayoutGateway)
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/transaction" {
                    MobileVendorTransactionView(vendorId: vendorId, vendorTransactionGateway: vendorTransactionGateway)
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "attachment" || vendorContentRoute == "vendor/attachment" {
                    MobileAttachmentView(vendorId: vendorId, attachmentFeatureBridge: attachmentFeatureBridge)
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/product/new" {
                    VendorNewCrudView(singular: "Product", resource: "product", listRoute: "vendor/product", fields: ProductNewFields, gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/order/new" {
                    VendorNewCrudView(singular: "Order", resource: "order", listRoute: "vendor/order", fields: OrderNewFields, gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/project/new" {
                    ProjectNewWizardView(gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/product" || vendorContentRoute.hasPrefix("vendor/product/") {
                    VendorOwnedCrudView(title: "Products", resource: "product", routeRoot: "vendor/product", selectedId: selectedIdentity(routeRoot: "vendor/product"), gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/order" || vendorContentRoute.hasPrefix("vendor/order/") {
                    VendorOwnedCrudView(title: "Orders", resource: "order", routeRoot: "vendor/order", selectedId: selectedIdentity(routeRoot: "vendor/order"), gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else if vendorContentRoute == "vendor/project" || vendorContentRoute.hasPrefix("vendor/project/") {
                    VendorOwnedCrudView(title: "Projects", resource: "project", routeRoot: "vendor/project", selectedId: selectedIdentity(routeRoot: "vendor/project"), gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                } else {
                    content(title: "Vendor", items: vendorContentRoute == "vendor" ? shell.vendorContext : [])
                        .toolbar { accountToolbar }
                }
            }
            .tabItem { Label(navigationLabelResolver("vendor", "vendor", "Vendor"), systemImage: "storefront") }
            .tag("vendor")

            NavigationView {
                if selectedRoute == "catalog" {
                    MobileCatalogView(catalogFeatureBridge: catalogFeatureBridge)
                        .toolbar { accountToolbar }
                } else {
                    content(title: "More", items: shell.moreDrawer)
                        .toolbar { accountToolbar }
                }
            }
            .tabItem { Label("More", systemImage: "line.3.horizontal") }
            .tag("more")
        }
        .tint(Color(red: 51 / 255, green: 51 / 255, blue: 51 / 255))
        .sheet(isPresented: $navigationOpen) {
            menuSheet(items: shell.moreDrawer) { item in
                handle(item)
                navigationOpen = false
            }
        }
        .sheet(isPresented: $accountOpen) {
            menuSheet(items: shell.accountQuick) { item in
                handle(item)
                accountOpen = false
            }
        }
        .confirmationDialog("New", isPresented: $newChooserOpen, titleVisibility: .visible) {
            Button("Product") {
                vendorContentRoute = "vendor/product/new"
                selectedRoute = "vendor"
            }
            Button("Order") {
                vendorContentRoute = "vendor/order/new"
                selectedRoute = "vendor"
            }
            Button("Project") {
                vendorContentRoute = "vendor/project/new"
                selectedRoute = "vendor"
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Choose what you want to add.")
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
        ToolbarItem(placement: .navigationBarLeading) {
            Button {
                navigationOpen = true
            } label: {
                Image(systemName: "line.3.horizontal")
            }
            .accessibilityLabel("Open navigation")
        }
        if selectedRoute == "vendor" || vendorContentRoute.hasPrefix("vendor/") {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { newChooserOpen = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel("New")
            }
        }
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

    private func selectedIdentity(routeRoot: String) -> String? {
        guard vendorContentRoute.hasPrefix(routeRoot + "/") else { return nil }
        return vendorContentRoute.split(separator: "/").dropFirst(2).first.map(String.init)
    }

    private func vendorOwnedRouteView(title: String, routeRoot: String) -> some View {
        let isDetail = vendorContentRoute != routeRoot
        return List {
            Section {
                Text(isDetail ? "\(title.dropLast()) Detail" : title)
                    .font(.headline)
                Text(isDetail ? vendorContentRoute : "Select an item to open its detail view.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                if isDetail {
                    Button("Back to \(title)") { vendorContentRoute = routeRoot }
                }
            }
            if routeRoot == "vendor/order", isDetail {
                Section("Order") {
                    let orderId = vendorContentRoute.split(separator: "/").dropFirst(2).first.map(String.init) ?? ""
                    Button("Shipments") { vendorContentRoute = "vendor/order/\(orderId)/shipment" }
                    Button("Tax") { vendorContentRoute = "vendor/order/\(orderId)/tax" }
                }
            }
        }
        .navigationTitle(isDetail ? "\(title.dropLast()) Detail" : title)
    }

    private func menuSheet(
        items: [MobileNavigationItemPayload],
        onSelect: @escaping (MobileNavigationItemPayload) -> Void
    ) -> some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(items.filter { $0.visible }) { item in
                    Button {
                        onSelect(item)
                    } label: {
                        row(item)
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(Color(.systemBackground))
                            )
                            .shadow(color: Color.black.opacity(0.06), radius: 8, y: 2)
                    }
                    .buttonStyle(.plain)
                    .disabled(!item.enabled)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 24)
        }
        .background(Color(red: 244 / 255, green: 245 / 255, blue: 246 / 255))
    }

    private func row(_ item: MobileNavigationItemPayload) -> some View {
        HStack(spacing: 12) {
            Image(systemName: systemImage(for: item))
                .foregroundColor(item.enabled ? Color(red: 51 / 255, green: 51 / 255, blue: 51 / 255) : .secondary)
                .frame(width: 24)

            VStack(alignment: .leading, spacing: 2) {
                Text(navigationLabelResolver(item.route, item.key, item.label))
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
        if MobileRouteResolver.isSignOutAction(action: item.action, route: item.route) {
            onSignOut()
            return
        }

        guard item.enabled, let route = item.route else {
            return
        }

        guard isHandledRoute(route), route != "catalog" || catalogEnabled else {
            return
        }

        if route == "attachment" {
            vendorContentRoute = "attachment"
            selectedRoute = "vendor"
            accountOpen = false
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
        MobileRouteResolver.isCurrentlyRenderable(route)
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
                item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
                item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action: "access.sign_out"),
            ],
            moreDrawer: [
                item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
                item("vendor", "Vendor", "store", true, "vendor"),
                item("catalog", "Catalog", "catalog", false, "catalog"),
                item("message", "Message", "message", false, "message"),
                item("attachment", "Attachment", "attachment", true, "attachment"),
            ],
            vendorContext: [
                item("vendor_overview", "My Vendor", "store", true, "vendor"),
                item("vendor_profile", "My Profile", "person", true, "vendor/profile"),
                item("vendor_summary", "Summary", "summary", true, "vendor/summary"),
                item("vendor_statement", "Statement", "statement", true, "vendor/statement"),
                item("vendor_payout", "Payout", "payout", true, "vendor/payout"),
                item("vendor_transaction", "Transaction", "receipt", true, "vendor/transaction"),
                item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
                item("vendor_product", "Products", "catalog", true, "vendor/product"),
                item("vendor_order", "Orders", "statement", true, "vendor/order"),
                item("vendor_project", "Projects", "summary", true, "vendor/project"),
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




