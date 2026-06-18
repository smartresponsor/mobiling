package com.smartresponsor.mobile.app.dashboard

/** Canonical Dashboard ownership and navigation map. */
class DashboardRouteMap {
    fun primarySections(): List<DashboardNavigationSection> = listOf(
        DashboardNavigationSection.CATALOG,
        DashboardNavigationSection.MESSAGE,
        DashboardNavigationSection.VENDOR,
    )

    fun entryFlows(): List<DashboardEntryFlow> = listOf(DashboardEntryFlow.AUTH)

    fun navigationOwners(): Map<String, String> = mapOf(
        "catalog" to "catalog/navigation",
        "vendor" to "vendor/navigation",
        "order" to "order/ownership",
    )
}
