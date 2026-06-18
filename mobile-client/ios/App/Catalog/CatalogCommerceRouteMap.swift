import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Canonical catalog commerce route map.
public struct CatalogCommerceRouteMap: Sendable {
    public init() {}

    public func primaryFlows() -> [CatalogCommerceRoute] {
        [.vendor, .product]
    }
}
