import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Canonical dashboard route map.
public struct DashboardRouteMap: Sendable {
    public init() {}

    public func primarySections() -> [DashboardPrimarySection] {
        [.catalog, .message, .vendor, .auth]
    }

    public func secondaryFlows() -> [DashboardSecondaryFlow] {
        [.product]
    }

    public func navigationOwners() -> [String: String] {
        [
            "catalog": "catalog/navigation",
            "vendor": "vendor/navigation",
            "order": "order/ownership",
        ]
    }
}
