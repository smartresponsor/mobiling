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
public enum MobileTextKey: String {
    case dashboard = "navigation.dashboard"
    case catalog = "navigation.catalog"
    case message = "navigation.message"
    case vendor = "navigation.vendor"
}
public struct MobileTextResolver {
    private let localText: [String: String]
    public init(localText: [String: String]) { self.localText = localText }
    public func resolve(semanticKey: String?, backendLabel: String) -> String {
        guard let semanticKey, let value = localText[semanticKey], !value.isEmpty else { return backendLabel }
        return value
    }
}
public struct MobileApplicationConfiguration {
    public let product: ProductProfile
    public let brand: BrandProfile
    public let environment: EnvironmentProfile
    public let initialDestination: InitialDestinationPolicy
    public let catalog: CatalogPolicy
    public let textResolver: MobileTextResolver
    public init(product: ProductProfile, brand: BrandProfile, environment: EnvironmentProfile, initialDestination: InitialDestinationPolicy, catalog: CatalogPolicy, textResolver: MobileTextResolver) {
        self.product = product
        self.brand = brand
        self.environment = environment
        self.initialDestination = initialDestination
        self.catalog = catalog
        self.textResolver = textResolver
    }
}
