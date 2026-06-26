package app.mobiling.client.ui.navigation.shell

import app.mobiling.client.contract.navigation.shell.MobileNavigationItemPayload
import app.mobiling.client.contract.navigation.shell.MobileNavigationShellPayload

data class MobileNavigationShellScreenContract(
    val bottomPrimary: List<MobileNavigationItemPayload>,
    val accountQuick: List<MobileNavigationItemPayload>,
    val moreDrawer: List<MobileNavigationItemPayload>,
    val vendorContext: List<MobileNavigationItemPayload>,
) {
    companion object {
        fun from(payload: MobileNavigationShellPayload): MobileNavigationShellScreenContract = MobileNavigationShellScreenContract(
            bottomPrimary = payload.items("mobile.bottom.primary"),
            accountQuick = payload.items("mobile.account.quick"),
            moreDrawer = payload.items("mobile.more.drawer"),
            vendorContext = payload.items("mobile.vendor.context"),
        )
    }
}
