import SwiftUI

private enum GuestRoute: String, CaseIterable {
    case home
    case catalog
    case users
    case orders
    case cart

    var title: String {
        switch self {
        case .home: return "Home"
        case .catalog: return "Catalog"
        case .users: return "Users"
        case .orders: return "Orders"
        case .cart: return "Cart"
        }
    }

    var systemImage: String {
        switch self {
        case .home: return "house"
        case .catalog: return "square.grid.2x2"
        case .users: return "person.2"
        case .orders: return "doc.text"
        case .cart: return "cart"
        }
    }

    static func resolve(_ rawRoute: String) -> GuestRoute {
        GuestRoute(rawValue: rawRoute) ?? .home
    }
}

struct AccessWelcomeView: View {
    private let catalogFeatureBridge: CatalogFeatureBridge?
    private let cartFeatureBridge: CartFeatureBridge?
    private let onSignIn: () -> Void
    private let onCreateAccess: () -> Void

    @State private var selectedRoute: GuestRoute

    init(
        initialRoute: String = "home",
        catalogFeatureBridge: CatalogFeatureBridge? = nil,
        cartFeatureBridge: CartFeatureBridge? = nil,
        onSignIn: @escaping () -> Void,
        onCreateAccess: @escaping () -> Void
    ) {
        self.catalogFeatureBridge = catalogFeatureBridge
        self.cartFeatureBridge = cartFeatureBridge
        self.onSignIn = onSignIn
        self.onCreateAccess = onCreateAccess
        self._selectedRoute = State(initialValue: GuestRoute.resolve(initialRoute))
    }

    var body: some View {
        TabView(selection: $selectedRoute) {
            NavigationView {
                homeView
                    .navigationTitle("1tasker")
            }
            .tabItem { Label(GuestRoute.home.title, systemImage: GuestRoute.home.systemImage) }
            .tag(GuestRoute.home)

            NavigationView {
                CatalogMobileScreen(catalogFeatureBridge: catalogFeatureBridge)
            }
            .tabItem { Label(GuestRoute.catalog.title, systemImage: GuestRoute.catalog.systemImage) }
            .tag(GuestRoute.catalog)

            NavigationView {
                guestPlaceholder(
                    title: "Users",
                    description: "Public customer, specialist, vendor, and sponsor profiles will be available here without exposing private account data."
                )
            }
            .tabItem { Label(GuestRoute.users.title, systemImage: GuestRoute.users.systemImage) }
            .tag(GuestRoute.users)

            NavigationView {
                guestPlaceholder(
                    title: "Orders",
                    description: "Guests can start checkout activity here. Personal order history remains available only after authentication."
                )
            }
            .tabItem { Label(GuestRoute.orders.title, systemImage: GuestRoute.orders.systemImage) }
            .tag(GuestRoute.orders)

            NavigationView {
                CartMobileScreen(cartFeatureBridge: cartFeatureBridge)
            }
            .tabItem { Label(GuestRoute.cart.title, systemImage: GuestRoute.cart.systemImage) }
            .tag(GuestRoute.cart)
        }
    }

    private var homeView: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xl) {
                Text("Find work, services, and products from one marketplace.")
                    .font(.title2.weight(.semibold))

                Text("Browse publicly now, or sign in to manage your tasks, messages, services, and profile.")
                    .foregroundStyle(.secondary)

                Button("Sign in", action: onSignIn)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)

                Button("Create account", action: onCreateAccess)
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(MobileDesignDefaults.Spacing.xxl)
        }
        .background(Color(.systemGroupedBackground))
    }

    private func guestPlaceholder(title: String, description: String) -> some View {
        List {
            Section {
                CanonicalStateCard(title: title, description: description)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
            }
        }
        .navigationTitle(title)
    }
}
