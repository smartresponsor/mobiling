import XCTest
@testable import CoreConfig

final class MobileApplicationConfigurationTests: XCTestCase {
    func testSemanticTextFallsBackToBackendLabel() {
        let resolver = MobileTextResolver(localText: [MobileTextKey.catalog.rawValue: "Catalog"])
        XCTAssertEqual(resolver.resolve(semanticKey: MobileTextKey.catalog.rawValue, backendLabel: "Backend catalog"), "Catalog")
        XCTAssertEqual(resolver.resolve(semanticKey: MobileTextKey.vendor.rawValue, backendLabel: "Backend vendor"), "Backend vendor")
        XCTAssertEqual(resolver.resolve(semanticKey: nil, backendLabel: "Backend label"), "Backend label")
    }

    func testPrimaryCatalogIsEnabled() {
        let policy = CatalogPolicy(primaryCatalog: "service", enabledCatalogs: ["service", "product", "project"])
        XCTAssertTrue(policy.enabledCatalogs.contains(policy.primaryCatalog))
    }
}
