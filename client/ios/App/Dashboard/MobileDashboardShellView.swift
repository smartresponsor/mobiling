import SwiftUI

public struct MobileDashboardShellView: View {
    private let navigationShellGateway: NavigationShellGateway?
    private let messageFeatureBridge: MessageFeatureBridge?
    private let notificationFeatureBridge: NotificationFeatureBridge?
    private let supportFeatureBridge: SupportFeatureBridge?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?
    private let cartFeatureBridge: CartFeatureBridge?
    private let catalogFeatureBridge: CatalogFeatureBridge?
    private let vendorId: String?
    private let vendorProfileGateway: VendorProfileGateway?
    private let vendorSummaryGateway: VendorSummaryGateway?
    private let vendorStatementGateway: VendorStatementGateway?
    private let vendorPayoutGateway: VendorPayoutGateway?
    private let vendorTransactionGateway: VendorTransactionGateway?
    private let vendorCrudGateway: VendorCrudGateway?
    private let retailPlacementGateway: RetailPlacementGateway?
    private let walletGateway: WalletGateway?
    private let initialRoute: String
    private let catalogEnabled: Bool
    private let availableRetailKinds: [RetailKind]
    private let navigationLabelResolver: (String?, String, String) -> String
    private let onSignOut: () -> Void

    @State private var selectedRoute: String
    @State private var vendorContentRoute: String = "vendor"
    @State private var navigationOpen: Bool = false
    @State private var accountOpen: Bool = false
    @State private var moneyOpen: Bool = false
    @State private var cartOpen: Bool = false
    @State private var catalogOpen: Bool = false
    @State private var supportOpen: Bool = false
    @State private var supportRoute: String = "support"
    @State private var vendorToolOpen: Bool = false
    @State private var vendorToolRoute: String = "vendor/summary"
    @State private var newChooserOpen: Bool = false
    @State private var selectedRetailKind: RetailKind
    @State private var shell: MobileNavigationShellScreenContract = MobileDashboardShellView.fallbackShell()

    public init(
        navigationShellGateway: NavigationShellGateway? = nil,
        messageFeatureBridge: MessageFeatureBridge? = nil,
        notificationFeatureBridge: NotificationFeatureBridge? = nil,
        supportFeatureBridge: SupportFeatureBridge? = nil,
        attachmentFeatureBridge: AttachmentFeatureBridge? = nil,
        cartFeatureBridge: CartFeatureBridge? = nil,
        catalogFeatureBridge: CatalogFeatureBridge? = nil,
        vendorId: String? = nil,
        vendorProfileGateway: VendorProfileGateway? = nil,
        vendorSummaryGateway: VendorSummaryGateway? = nil,
        vendorStatementGateway: VendorStatementGateway? = nil,
        vendorPayoutGateway: VendorPayoutGateway? = nil,
        vendorTransactionGateway: VendorTransactionGateway? = nil,
        vendorCrudGateway: VendorCrudGateway? = nil,
        retailPlacementGateway: RetailPlacementGateway? = nil,
        walletGateway: WalletGateway? = nil,
        initialRoute: String = "vendor/project",
        catalogEnabled: Bool = true,
        availableRetailKinds: [RetailKind] = RetailKind.allCases,
        navigationLabelResolver: @escaping (String?, String, String) -> String = { _, _, label in label },
        onSignOut: @escaping () -> Void
    ) {
        self.navigationShellGateway = navigationShellGateway
        self.messageFeatureBridge = messageFeatureBridge
        self.notificationFeatureBridge = notificationFeatureBridge
        self.supportFeatureBridge = supportFeatureBridge
        self.attachmentFeatureBridge = attachmentFeatureBridge
        self.cartFeatureBridge = cartFeatureBridge
        self.catalogFeatureBridge = catalogFeatureBridge
        self.vendorId = vendorId
        self.vendorProfileGateway = vendorProfileGateway
        self.vendorSummaryGateway = vendorSummaryGateway
        self.vendorStatementGateway = vendorStatementGateway
        self.vendorPayoutGateway = vendorPayoutGateway
        self.vendorTransactionGateway = vendorTransactionGateway
        self.vendorCrudGateway = vendorCrudGateway
        self.retailPlacementGateway = retailPlacementGateway
        self.walletGateway = walletGateway
        self.initialRoute = initialRoute
        self.catalogEnabled = catalogEnabled
        self.availableRetailKinds = availableRetailKinds
        self.navigationLabelResolver = navigationLabelResolver
        self._selectedRoute = State(initialValue: MobileDashboardShellView.initialBottomRoute(initialRoute))
        self._cartOpen = State(initialValue: MobileRouteResolver.normalizeRoute(initialRoute) == "cart")
        self._selectedRetailKind = State(initialValue: availableRetailKinds[0])
        self.onSignOut = onSignOut
    }

    public var body: some View {
        TabView(selection: $selectedRoute) {
            NavigationView {
                VendorOwnedCrudView(title: "Tasks", resource: "project", routeRoot: "vendor/project", selectedId: selectedIdentity(routeRoot: "vendor/project"), gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                    .toolbar { accountToolbar }
            }
            .tabItem { Label(navigationLabelResolver("vendor/project", "tasks", "Tasks"), systemImage: "list.bullet.rectangle") }
            .tag("vendor/project")

            NavigationView {
                MessageMobileScreen(
                    messageFeatureBridge: messageFeatureBridge,
                    vendorId: vendorId,
                    attachmentFeatureBridge: attachmentFeatureBridge
                )
                    .toolbar { accountToolbar }
            }
            .tabItem { Label(navigationLabelResolver("message", "message", "Messages"), systemImage: "message") }
            .tag("message")

            NavigationView {
                if vendorContentRoute == "vendor/retail/new" {
                    VendorNewCrudView(
                        singular: retailKindLabel(selectedRetailKind),
                        resource: "retail",
                        listRoute: "vendor/retail",
                        fields: RetailNewFields,
                        gateway: vendorCrudGateway,
                        catalogFeatureBridge: catalogFeatureBridge,
                        onRouteSelected: { vendorContentRoute = $0 },
                        createdRoute: { "vendor/retail/\($0)/placement" },
                        initialValues: ["kind": selectedRetailKind.rawValue, "currency": "USD"],
                        availableRetailKinds: availableRetailKinds
                    )
                    .toolbar { accountToolbar }
                } else if let retailId = retailPlacementIdentity() {
                    RetailPlacementView(
                        retailId: retailId,
                        gateway: retailPlacementGateway,
                        onRouteSelected: { vendorContentRoute = $0 }
                    )
                    .toolbar { accountToolbar }
                } else {
                    VendorOwnedCrudView(title: "Services", resource: "retail", routeRoot: "vendor/retail", selectedId: selectedIdentity(routeRoot: "vendor/retail"), gateway: vendorCrudGateway, onRouteSelected: { vendorContentRoute = $0 })
                        .toolbar { accountToolbar }
                }
            }
            .tabItem { Label(navigationLabelResolver("vendor/retail", "services", "Services"), systemImage: "storefront") }
            .tag("vendor/retail")

            NavigationView {
                NotificationMobileScreen(notificationFeatureBridge: notificationFeatureBridge)
                    .toolbar { accountToolbar }
            }
            .tabItem { Label(navigationLabelResolver("notification", "notification", "Notifications"), systemImage: "bell") }
            .tag("notification")

            NavigationView {
                MobileVendorProfileView(vendorId: vendorId, vendorProfileGateway: vendorProfileGateway, attachmentFeatureBridge: attachmentFeatureBridge)
                    .toolbar { accountToolbar }
            }
            .tabItem { Label(navigationLabelResolver("vendor/page", "profile", "Profile"), systemImage: "person.crop.circle") }
            .tag("vendor/page")
        }
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
        .sheet(isPresented: $moneyOpen) {
            NavigationView {
                moneyView
            }
        }
        .sheet(isPresented: $cartOpen) {
            NavigationView {
                CartMobileScreen(cartFeatureBridge: cartFeatureBridge)
            }
        }
        .sheet(isPresented: $catalogOpen) {
            NavigationView {
                CatalogMobileScreen(catalogFeatureBridge: catalogFeatureBridge)
            }
        }
        .sheet(isPresented: $supportOpen) {
            NavigationView {
                SupportMobileScreen(
                    route: supportRoute,
                    supportFeatureBridge: supportFeatureBridge,
                    onRouteSelected: { route in supportRoute = route }
                )
            }
        }
        .sheet(isPresented: $vendorToolOpen) {
            NavigationView {
                vendorToolView
            }
        }
        .confirmationDialog("New", isPresented: $newChooserOpen, titleVisibility: .visible) {
            ForEach(availableRetailKinds, id: \.rawValue) { kind in
                Button(retailKindLabel(kind)) {
                    selectedRetailKind = kind
                    vendorContentRoute = "vendor/retail/new"
                    selectedRoute = "vendor/retail"
                }
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
        if selectedRoute == "vendor/retail" {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { newChooserOpen = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel("New")
            }
        }
        ToolbarItem(placement: .navigationBarTrailing) {
            Button("Account") { accountOpen = true }
        }
    }

    private static func initialBottomRoute(_ route: String) -> String {
        switch MobileRouteResolver.normalizeRoute(route) {
        case "message", "notification", "vendor/page", "vendor/retail", "vendor/project": return MobileRouteResolver.normalizeRoute(route)
        default: return "vendor/project"
        }
    }

    @ViewBuilder
    private var vendorToolView: some View {
        switch vendorToolRoute {
        case "attachment", "vendor/attachment":
            MobileAttachmentView(vendorId: vendorId, attachmentFeatureBridge: attachmentFeatureBridge)
        case "vendor/order":
            VendorOwnedCrudView(
                title: "Orders",
                resource: "order",
                routeRoot: "vendor/order",
                selectedId: selectedIdentity(routeRoot: "vendor/order"),
                gateway: vendorCrudGateway,
                onRouteSelected: { vendorContentRoute = $0 }
            )
        case "vendor/summary":
            MobileVendorSummaryView(vendorId: vendorId, vendorSummaryGateway: vendorSummaryGateway)
        case "vendor/statement":
            MobileVendorStatementView(vendorId: vendorId, vendorStatementGateway: vendorStatementGateway)
        case "vendor/payout":
            MobileVendorPayoutView(vendorId: vendorId, vendorPayoutGateway: vendorPayoutGateway)
        case "vendor/transaction":
            MobileVendorTransactionView(vendorId: vendorId, vendorTransactionGateway: vendorTransactionGateway)
        default:
            Text("Vendor surface is not available.")
        }
    }

    private var moneyView: some View {
        List {
            Section {
                NavigationLink {
                    CartMobileScreen(cartFeatureBridge: cartFeatureBridge)
                } label: {
                    moneyRow("Cart", systemImage: "cart", description: "Review items before checkout.", enabled: true)
                }
                NavigationLink {
                    WalletOverviewView(gateway: walletGateway)
                } label: {
                    moneyRow("Wallet", systemImage: "wallet.pass", description: "Balances, reservations and wallet activity.", enabled: true)
                }
                moneyRow("Billing", systemImage: "doc.text", description: "Bills, invoices and billing history.", enabled: false)
                moneyRow("Payments", systemImage: "creditcard", description: "Payment activity and payment methods.", enabled: false)
                moneyRow("Finance", systemImage: "chart.bar", description: "Financial summaries and reporting.", enabled: false)
            }
        }
        .navigationTitle("Money")
    }

    private func moneyRow(_ title: String, systemImage: String, description: String, enabled: Bool) -> some View {
        HStack(spacing: MobileDesignDefaults.Spacing.md) {
            Image(systemName: systemImage)
                .frame(width: MobileDesignDefaults.Spacing.xxl)
            VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                Text(title).font(.headline)
                Text(description).font(.footnote).foregroundStyle(.secondary)
            }
            Spacer()
            if !enabled {
                Text("Coming soon")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .opacity(enabled ? 1.0 : 0.55)
    }

    private func retailPlacementIdentity() -> String? {
        let segments = vendorContentRoute.split(separator: "/").map(String.init)
        guard segments.count == 4,
              segments[0] == "vendor",
              segments[1] == "retail",
              segments[3] == "placement" else { return nil }
        return segments[2]
    }

    private func selectedIdentity(routeRoot: String) -> String? {
        guard vendorContentRoute.hasPrefix(routeRoot + "/") else { return nil }
        return vendorContentRoute.split(separator: "/").dropFirst(2).first.map(String.init)
    }

    private func menuSheet(
        items: [MobileNavigationItemPayload],
        onSelect: @escaping (MobileNavigationItemPayload) -> Void
    ) -> some View {
        ScrollView {
            LazyVStack(spacing: MobileDesignDefaults.Spacing.md) {
                ForEach(items.filter { $0.visible }) { item in
                    Button {
                        onSelect(item)
                    } label: {
                        row(item)
                            .padding(MobileDesignDefaults.Spacing.lg)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: MobileDesignDefaults.Spacing.lg, style: .continuous)
                                    .fill(Color(.systemBackground))
                            )
                            .shadow(color: Color.black.opacity(0.06), radius: 8, y: 2)
                    }
                    .buttonStyle(.plain)
                    .disabled(!item.enabled)
                }
            }
            .padding(.horizontal, MobileDesignDefaults.Spacing.xl)
            .padding(.top, MobileDesignDefaults.Spacing.sm)
            .padding(.bottom, MobileDesignDefaults.Spacing.xxl)
        }
        .background(Color(.systemGroupedBackground))
    }

    private func row(_ item: MobileNavigationItemPayload) -> some View {
        HStack(spacing: MobileDesignDefaults.Spacing.md) {
            Image(systemName: systemImage(for: item))
                .foregroundColor(item.enabled ? Color.accentColor : .secondary)
                .frame(width: MobileDesignDefaults.Spacing.xxl)

            Text(displayLabel(for: item))

            Spacer()

            if !item.enabled {
                Text("Coming soon")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, MobileDesignDefaults.Spacing.sm)
                    .padding(.vertical, MobileDesignDefaults.Spacing.xs)
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

        if route == "money" {
            moneyOpen = true
            accountOpen = false
            return
        }

        if route == "cart" {
            cartOpen = true
            accountOpen = false
            navigationOpen = false
            return
        }

        if route == "catalog", catalogEnabled {
            catalogOpen = true
            accountOpen = false
            navigationOpen = false
            return
        }

        if route == "support" || route.hasPrefix("support/") {
            supportRoute = route
            supportOpen = true
            accountOpen = false
            navigationOpen = false
            return
        }

        if ["attachment", "vendor/attachment", "vendor/order", "vendor/summary", "vendor/statement", "vendor/payout", "vendor/transaction"].contains(route) {
            vendorToolRoute = route
            vendorToolOpen = true
            accountOpen = false
            navigationOpen = false
            return
        }

        if route == "vendor" || route == "vendor/page" {
            vendorContentRoute = "vendor/page"
            selectedRoute = "vendor/page"
            accountOpen = false
            return
        }

        if route == "vendor/project" || route.hasPrefix("vendor/project/") {
            vendorContentRoute = route
            selectedRoute = "vendor/project"
            accountOpen = false
            return
        }

        if route == "vendor/retail" || route.hasPrefix("vendor/retail/") {
            vendorContentRoute = route
            selectedRoute = "vendor/retail"
            accountOpen = false
            return
        }

        if route == "message" || route == "notification" {
            selectedRoute = route
            accountOpen = false
            return
        }

        selectedRoute = route
        accountOpen = false
    }

    private func displayLabel(for item: MobileNavigationItemPayload) -> String {
        navigationLabelResolver(MobileRouteResolver.normalizeRoute(item.route), item.key, item.label)
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
        case "tasks": return "list.bullet.rectangle"
        case "notification": return "bell"
        case "wallet": return "wallet.pass"
        case "support": return "questionmark.circle"
        default: return "house"
        }
    }

    private func retailKindLabel(_ kind: RetailKind) -> String {
        switch kind {
        case .task: return "Task"
        case .service: return "Service"
        case .goods: return "Product"
        case .project: return "Project"
        }
    }

    private func isHandledRoute(_ route: String) -> Bool {
        MobileRouteResolver.isCurrentlyRenderable(route)
    }

    private static func fallbackShell() -> MobileNavigationShellScreenContract {
        MobileNavigationShellScreenContract(
            bottomPrimary: [
                item("tasks", "Tasks", "tasks", true, "vendor/project"),
                item("message", "Messages", "message", true, "message"),
                item("services", "Services", "store", true, "vendor/retail"),
                item("notification", "Notifications", "notification", true, "notification"),
                item("vendor_page", "Profile", "person", true, "vendor/page"),
            ],
            accountQuick: [
                item("vendor_page", "Profile", "person", true, "vendor/page"),
                item("access_password", "Change Password", "key", false, "access/password"),
                item("access_verification", "Verification", "key", false, "access/verification"),
                item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
                item("casing_cases", "My cases", "support", true, "support/case"),
                item("casing_order_help", "Order help", "receipt", true, "support/order"),
                item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action: "access.sign_out"),
            ],
            moreDrawer: [
                item("dashboard", "Dashboard", "dashboard", false, "dashboard"),
                item("money", "Money", "wallet", true, "money"),
                item("tasks", "Tasks", "tasks", true, "vendor/project"),
                item("message", "Messages", "message", true, "message"),
                item("services", "Services", "store", true, "vendor/retail"),
                item("notification", "Notifications", "notification", true, "notification"),
                item("support", "Support", "support", true, "support"),
                item("vendor_page", "Profile", "person", true, "vendor/page"),
                item("catalog", "Catalog", "catalog", false, "catalog"),
                item("attachment", "Attachment", "attachment", true, "attachment"),
            ],
            vendorContext: [
                item("vendor_overview", "Profile Overview", "person", true, "vendor"),
                item("vendor_page", "Profile", "person", true, "vendor/page"),
                item("vendor_summary", "Summary", "summary", true, "vendor/summary"),
                item("vendor_statement", "Statement", "statement", true, "vendor/statement"),
                item("vendor_payout", "Payout", "payout", true, "vendor/payout"),
                item("vendor_transaction", "Transaction", "receipt", true, "vendor/transaction"),
                item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
                item("vendor_product", "Services", "catalog", true, "vendor/retail"),
                item("vendor_order", "Orders", "statement", true, "vendor/order"),
                item("vendor_project", "Tasks", "tasks", true, "vendor/project"),
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




