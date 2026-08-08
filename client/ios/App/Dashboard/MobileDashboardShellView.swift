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
    private let availableRetailKinds: [RetailKind]
    private let navigationLabelResolver: (String?, String, String) -> String
    private let onSignOut: () -> Void

    @State private var selectedRoute: String
    @State private var vendorContentRoute: String = "vendor"
    @State private var navigationOpen: Bool = false
    @State private var accountOpen: Bool = false
    @State private var newChooserOpen: Bool = false
    @State private var selectedRetailKind: RetailKind
    @State private var shell: MobileNavigationShellScreenContract = MobileDashboardShellView.fallbackShell()

    public init(navigationShellGateway: NavigationShellGateway? = nil, attachmentFeatureBridge: AttachmentFeatureBridge? = nil, catalogFeatureBridge: CatalogFeatureBridge? = nil, vendorId: String? = nil, vendorProfileGateway: VendorProfileGateway? = nil, vendorSummaryGateway: VendorSummaryGateway? = nil, vendorStatementGateway: VendorStatementGateway? = nil, vendorPayoutGateway: VendorPayoutGateway? = nil, vendorTransactionGateway: VendorTransactionGateway? = nil, vendorCrudGateway: VendorCrudGateway? = nil, initialRoute: String = "vendor/project", catalogEnabled: Bool = true, availableRetailKinds: [RetailKind] = RetailKind.allCases, navigationLabelResolver: @escaping (String?, String, String) -> String = { _, _, label in label }, onSignOut: @escaping () -> Void) {
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
        self.availableRetailKinds = availableRetailKinds
        self.navigationLabelResolver = navigationLabelResolver
        self._selectedRoute = State(initialValue: MobileDashboardShellView.initialBottomRoute(initialRoute))
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
                comingSoon(title: "Messages", systemImage: "message", description: "Task and customer conversations will appear here.")
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
                        onRouteSelected: { vendorContentRoute = $0 },
                        initialValues: ["kind": selectedRetailKind.rawValue, "currency": "USD"],
                        availableRetailKinds: availableRetailKinds
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
                comingSoon(title: "Notifications", systemImage: "bell", description: "Important 1Tasker updates will appear here.")
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

    private func comingSoon(title: String, systemImage: String, description: String) -> some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 12) {
                    Image(systemName: systemImage)
                        .font(.system(size: 34, weight: .semibold))
                        .foregroundStyle(.secondary)
                    Text(title)
                        .font(.headline)
                    Text(description)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 18)
            }
        }
        .navigationTitle(title)
    }

    private func content(title: String, items: [MobileNavigationItemPayload]) -> some View {
        List {
            Section {
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

            Text(displayLabel(for: item))

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
                item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action: "access.sign_out"),
            ],
            moreDrawer: [
                item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
                item("tasks", "Tasks", "tasks", true, "vendor/project"),
                item("message", "Messages", "message", true, "message"),
                item("services", "Services", "store", true, "vendor/retail"),
                item("notification", "Notifications", "notification", true, "notification"),
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




