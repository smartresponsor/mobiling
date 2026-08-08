import Foundation
import CoreConfig

enum MobileClientRuntimeConfig {
    static let configuration = MobileApplicationConfiguration(
        product: ProductProfile(code: setting("MOBILE_PRODUCT_PROFILE", fallback: "one_tasker")),
        brand: BrandProfile(code: setting("MOBILE_BRAND_PROFILE", fallback: "one_tasker")),
        environment: EnvironmentProfile(
            code: setting("MOBILE_ENVIRONMENT_PROFILE", fallback: "local"),
            mobileEdgeBaseUrl: setting("MOBILE_EDGE_BASE_URL", fallback: "http://localhost:8080")
        ),
        initialDestination: InitialDestinationPolicy(destination: setting("MOBILE_INITIAL_DESTINATION", fallback: "dashboard")),
        catalog: CatalogPolicy(
            primaryCatalog: setting("MOBILE_PRIMARY_CATALOG", fallback: "services"),
            enabledCatalogs: Set(setting("MOBILE_ENABLED_CATALOGS", fallback: "services,products,projects").split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) })
        ),
        retail: RetailPolicy(
            availableKinds: setting("MOBILE_AVAILABLE_RETAIL_KINDS", fallback: "service,goods,project")
                .split(separator: ",")
                .compactMap { RetailKind(rawValue: $0.trimmingCharacters(in: .whitespaces)) }
        ),
        textResolver: MobileTextResolver(localText: [
            MobileTextKey.dashboard.rawValue: String(localized: "navigation.dashboard", defaultValue: "Dashboard"),
            MobileTextKey.catalog.rawValue: String(localized: "navigation.catalog", defaultValue: "Catalog"),
            MobileTextKey.message.rawValue: String(localized: "navigation.message", defaultValue: "Messages"),
            MobileTextKey.notification.rawValue: String(localized: "navigation.notification", defaultValue: "Notifications"),
            MobileTextKey.tasks.rawValue: String(localized: "navigation.tasks", defaultValue: "Tasks"),
            MobileTextKey.services.rawValue: String(localized: "navigation.services", defaultValue: "Services"),
            MobileTextKey.profile.rawValue: String(localized: "navigation.profile", defaultValue: "Profile"),
            MobileTextKey.vendor.rawValue: String(localized: "navigation.vendor", defaultValue: "Vendor"),
        ])
    )

    static let mobileEdgeBaseUrl = configuration.environment.mobileEdgeBaseUrl

    private static func setting(_ key: String, fallback: String) -> String {
        Bundle.main.object(forInfoDictionaryKey: key) as? String ?? fallback
    }
}
