import Foundation

public struct MobileNavigationShellScreenContract: Sendable {
    public let bottomPrimary: [MobileNavigationItemPayload]
    public let accountQuick: [MobileNavigationItemPayload]
    public let moreDrawer: [MobileNavigationItemPayload]
    public let vendorContext: [MobileNavigationItemPayload]

    public init(payload: MobileNavigationShellPayload) {
        self.bottomPrimary = payload.items(location: "mobile.bottom.primary")
        self.accountQuick = payload.items(location: "mobile.account.quick")
        self.moreDrawer = payload.items(location: "mobile.more.drawer")
        self.vendorContext = payload.items(location: "mobile.vendor.context")
    }

    public init(bottomPrimary: [MobileNavigationItemPayload], accountQuick: [MobileNavigationItemPayload], moreDrawer: [MobileNavigationItemPayload], vendorContext: [MobileNavigationItemPayload]) {
        self.bottomPrimary = bottomPrimary
        self.accountQuick = accountQuick
        self.moreDrawer = moreDrawer
        self.vendorContext = vendorContext
    }
}
