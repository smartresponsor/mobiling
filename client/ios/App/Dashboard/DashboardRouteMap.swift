import Foundation
public struct DashboardRouteMap: Sendable {
    public init() {}
    public func primarySections() -> [DashboardPrimarySection] { [.catalog, .message, .vendor] }
    public func entryFlows() -> [DashboardEntryFlow] { [.auth] }
    public func navigationOwners() -> [String: String] {
        ["catalog": "catalog/navigation", "vendor": "vendor/navigation", "order": "order/ownership"]
    }
}
