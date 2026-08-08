import Foundation

public struct ProductProfile: Equatable { public let code: String; public init(code: String) { self.code = code } }
public struct BrandProfile: Equatable { public let code: String; public init(code: String) { self.code = code } }
public struct EnvironmentProfile: Equatable {
    public let code: String
    public let mobileEdgeBaseUrl: String
    public init(code: String, mobileEdgeBaseUrl: String) { self.code = code; self.mobileEdgeBaseUrl = mobileEdgeBaseUrl }
}
public struct InitialDestinationPolicy: Equatable {
    public let destination: String
    public init(destination: String) { self.destination = destination }
    public func resolvedRoute(isRenderable: (String) -> Bool) -> String {
        let normalized = destination
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .split(separator: "/", omittingEmptySubsequences: true)
            .joined(separator: "/")
        return !normalized.isEmpty && isRenderable(normalized) ? normalized : "dashboard"
    }
}
public struct CatalogPolicy: Equatable {
    public let primaryCatalog: String
    public let enabledCatalogs: Set<String>
    public init(primaryCatalog: String, enabledCatalogs: Set<String>) {
        precondition(enabledCatalogs.contains(primaryCatalog), "Primary catalog must be enabled.")
        self.primaryCatalog = primaryCatalog
        self.enabledCatalogs = enabledCatalogs
    }
    public func isCatalogEnabled(_ catalog: String) -> Bool { enabledCatalogs.contains(catalog) }
    public var isPrimaryCatalogEnabled: Bool { isCatalogEnabled(primaryCatalog) }
}

public enum RetailKind: String, CaseIterable, Equatable {
    case task
    case service
    case goods
    case project
}

public struct RetailPolicy: Equatable {
    public let availableKinds: [RetailKind]
    public init(availableKinds: [RetailKind]) {
        precondition(!availableKinds.isEmpty, "At least one retail kind must be available.")
        self.availableKinds = availableKinds
    }
    public var defaultKind: RetailKind { availableKinds[0] }
    public func isAvailable(_ kind: RetailKind) -> Bool { availableKinds.contains(kind) }
}
public enum MobileTextKey: String, CaseIterable {
    case dashboard = "navigation.dashboard"
    case catalog = "navigation.catalog"
    case message = "navigation.message"
    case notification = "navigation.notification"
    case tasks = "navigation.tasks"
    case services = "navigation.services"
    case profile = "navigation.profile"
    case vendor = "navigation.vendor"

    var navigationRoot: String { String(rawValue.split(separator: ".").last ?? "") }
}
public struct MobileTextResolver {
    private let localText: [String: String]
    public init(localText: [String: String]) { self.localText = localText }
    public func resolve(semanticKey: String?, backendLabel: String) -> String {
        guard let semanticKey, let value = localText[semanticKey], !value.isEmpty else { return backendLabel }
        return value
    }
    public func resolveNavigation(route: String?, key: String, backendLabel: String) -> String {
        let normalizedRoute = route?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .split(separator: "/", omittingEmptySubsequences: true)
            .joined(separator: "/") ?? ""
        let normalizedKey = key.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()

        let semanticKey: String? = {
            switch (normalizedRoute, normalizedKey) {
            case ("vendor/project", _), (_, "tasks"), (_, "vendor_project"):
                return MobileTextKey.tasks.rawValue
            case ("vendor/retail", _), (_, "services"), (_, "vendor_product"):
                return MobileTextKey.services.rawValue
            case ("vendor/page", _), ("vendor/profile", _), (_, "profile"), (_, "vendor_page"), (_, "vendor_profile"):
                return MobileTextKey.profile.rawValue
            case ("message", _), (_, "message"):
                return MobileTextKey.message.rawValue
            case ("notification", _), (_, "notification"):
                return MobileTextKey.notification.rawValue
            default:
                let routeRoot = normalizedRoute.split(separator: "/", omittingEmptySubsequences: true).first.map(String.init)
                let keyRoot = normalizedKey.split(separator: "_", omittingEmptySubsequences: true).first.map(String.init)
                guard let root = routeRoot ?? keyRoot else { return nil }
                return MobileTextKey.allCases.first { $0.navigationRoot == root }?.rawValue
            }
        }()

        return resolve(semanticKey: semanticKey, backendLabel: backendLabel)
    }
}
public struct MobileApplicationConfiguration {
    public let product: ProductProfile
    public let brand: BrandProfile
    public let environment: EnvironmentProfile
    public let initialDestination: InitialDestinationPolicy
    public let catalog: CatalogPolicy
    public let retail: RetailPolicy
    public let textResolver: MobileTextResolver
    public init(product: ProductProfile, brand: BrandProfile, environment: EnvironmentProfile, initialDestination: InitialDestinationPolicy, catalog: CatalogPolicy, retail: RetailPolicy, textResolver: MobileTextResolver) {
        self.product = product
        self.brand = brand
        self.environment = environment
        self.initialDestination = initialDestination
        self.catalog = catalog
        self.retail = retail
        self.textResolver = textResolver
    }
}
