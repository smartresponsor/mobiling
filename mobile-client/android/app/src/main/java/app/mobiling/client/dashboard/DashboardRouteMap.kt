package app.mobiling.client.dashboard
class DashboardRouteMap {
    fun primarySections(): List<DashboardPrimarySection> = listOf(
        DashboardPrimarySection.CATALOG,
        DashboardPrimarySection.MESSAGE,
        DashboardPrimarySection.VENDOR,
    )
    fun entryFlows(): List<DashboardEntryFlow> = listOf(DashboardEntryFlow.AUTH)
    fun navigationOwners(): Map<String, String> = mapOf(
        "catalog" to "catalog/navigation",
        "vendor" to "vendor/navigation",
        "order" to "order/ownership",
    )
}
