import Foundation

public struct MobileNavigationShellScreenContract: Sendable {
    public let bottomPrimary: [MobileNavigationItemPayload]
    public let accountQuick: [MobileNavigationItemPayload]
    public let moreDrawer: [MobileNavigationItemPayload]
    public let vendorContext: [MobileNavigationItemPayload]

    public init(payload: MobileNavigationShellPayload) {
        self.bottomPrimary = Self.activateCatalog(in: payload.items(location: "mobile.bottom.primary"))
        self.accountQuick = payload.items(location: "mobile.account.quick")
        self.moreDrawer = Self.activateCatalog(in: payload.items(location: "mobile.more.drawer"))
        self.vendorContext = payload.items(location: "mobile.vendor.context")
    }

    public init(bottomPrimary: [MobileNavigationItemPayload], accountQuick: [MobileNavigationItemPayload], moreDrawer: [MobileNavigationItemPayload], vendorContext: [MobileNavigationItemPayload]) {
        self.bottomPrimary = Self.activateCatalog(in: bottomPrimary)
        self.accountQuick = accountQuick
        self.moreDrawer = Self.activateCatalog(in: moreDrawer)
        self.vendorContext = vendorContext
    }

    private static func activateCatalog(in items: [MobileNavigationItemPayload]) -> [MobileNavigationItemPayload] {
        items.map { item in
            guard item.route == "catalog" || item.key == "catalog" else {
                return item
            }

            return MobileNavigationItemPayload(
                id: item.id,
                key: item.key,
                label: item.label,
                icon: item.icon,
                badge: nil,
                enabled: true,
                visible: item.visible,
                status: "active",
                disabledReason: nil,
                requiredComponent: item.requiredComponent,
                location: item.location,
                group: item.group,
                groupLabel: item.groupLabel,
                action: item.action,
                route: "catalog"
            )
        }
    }
}
