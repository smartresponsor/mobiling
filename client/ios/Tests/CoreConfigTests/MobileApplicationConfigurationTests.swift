import XCTest
@testable import CoreConfig

final class MobileApplicationConfigurationTests: XCTestCase {
    func testSemanticTextFallsBackToBackendLabel() {
        let resolver = MobileTextResolver(localText: [MobileTextKey.catalog.rawValue: "Catalog"])
        XCTAssertEqual(resolver.resolve(semanticKey: MobileTextKey.catalog.rawValue, backendLabel: "Backend catalog"), "Catalog")
        XCTAssertEqual(resolver.resolve(semanticKey: MobileTextKey.vendor.rawValue, backendLabel: "Backend vendor"), "Backend vendor")
        XCTAssertEqual(resolver.resolve(semanticKey: nil, backendLabel: "Backend label"), "Backend label")
    }

    func testNavigationTextResolvesRouteSpecificPresentationKeys() {
        let resolver = MobileTextResolver(localText: [
            MobileTextKey.tasks.rawValue: "Local tasks",
            MobileTextKey.services.rawValue: "Local services",
            MobileTextKey.profile.rawValue: "Local profile",
        ])

        XCTAssertEqual(resolver.resolveNavigation(route: "vendor/project", key: "tasks", backendLabel: "Backend vendor"), "Local tasks")
        XCTAssertEqual(resolver.resolveNavigation(route: "vendor/retail", key: "services", backendLabel: "Backend vendor"), "Local services")
        XCTAssertEqual(resolver.resolveNavigation(route: "vendor/page", key: "vendor_page", backendLabel: "Backend vendor"), "Local profile")
    }

    func testPrimaryCatalogIsEnabled() {
        let policy = CatalogPolicy(primaryCatalog: "service", enabledCatalogs: ["service", "product", "project"])
        XCTAssertTrue(policy.enabledCatalogs.contains(policy.primaryCatalog))
    }
}
