import Foundation

public struct MobileNavigationShellPayload: Sendable {
    public let schema: String
    public let channel: String
    public let platforms: [String]
    public let locations: [String: [MobileNavigationItemPayload]]

    public func items(location: String) -> [MobileNavigationItemPayload] {
        locations[location] ?? []
    }
}

public struct MobileNavigationItemPayload: Identifiable, Sendable {
    public let id: String
    public let key: String
    public let label: String
    public let icon: String?
    public let badge: String?
    public let enabled: Bool
    public let visible: Bool
    public let status: String
    public let disabledReason: String?
    public let requiredComponent: String?
    public let location: String
    public let group: String
    public let groupLabel: String
    public let action: String?
    public let route: String?
