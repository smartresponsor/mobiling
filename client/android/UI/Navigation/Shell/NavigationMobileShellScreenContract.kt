package app.mobiling.client.ui.navigation.shell

import app.mobiling.client.contract.navigation.shell.NavigationMobileItemPayload
import app.mobiling.client.contract.navigation.shell.NavigationMobileShellPayload

data class NavigationMobileShellScreenContract(
    val bottomPrimary: List<NavigationMobileItemPayload>,
    val accountQuick: List<NavigationMobileItemPayload>,
    val moreDrawer: List<NavigationMobileItemPayload>,
    val vendorContext: List<NavigationMobileItemPayload>,
) {
    companion object {
        fun from(payload: NavigationMobileShellPayload): NavigationMobileShellScreenContract = NavigationMobileShellScreenContract(
            bottomPrimary = payload.items("mobile.bottom.primary"),
            accountQuick = payload.items("mobile.account.quick"),
            moreDrawer = payload.items("mobile.more.drawer"),
            vendorContext = payload.items("mobile.vendor.context"),
        )
    }
}
