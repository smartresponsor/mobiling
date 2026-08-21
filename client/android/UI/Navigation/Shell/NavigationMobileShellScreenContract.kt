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
            bottomPrimary = activateCatalog(payload.items("mobile.bottom.primary")),
            accountQuick = payload.items("mobile.account.quick"),
            moreDrawer = activateCatalog(payload.items("mobile.more.drawer")),
            vendorContext = payload.items("mobile.vendor.context"),
        )

        private fun activateCatalog(items: List<NavigationMobileItemPayload>): List<NavigationMobileItemPayload> =
            items.map { item ->
                if (item.route == "catalog" || item.key == "catalog") {
                    item.copy(
                        badge = null,
                        enabled = true,
                        status = "active",
                        disabledReason = null,
                        route = "catalog",
                    )
                } else {
                    item
                }
            }
    }
}
