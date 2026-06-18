package com.smartresponsor.mobile.app.dashboard

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical dashboard route map.
 *
 * It reflects the working spec where Dashboard is the only root section,
 * with primary sections and secondary flows owned by Catalog / Vendor /
 * Order chains rather than exposed as a flat surface.
 */
class DashboardRouteMap {
    fun primarySections(): List<DashboardPrimarySection> = listOf(
        DashboardPrimarySection.CATALOG,
        DashboardPrimarySection.MESSAGE,
        DashboardPrimarySection.VENDOR,
        DashboardPrimarySection.AUTH,
    )

    fun secondaryFlows(): List<DashboardSecondaryFlow> = listOf(
        DashboardSecondaryFlow.PRODUCT,
    )

    fun navigationOwners(): Map<String, String> = mapOf(
        "catalog" to "catalog/navigation",
        "vendor" to "vendor/navigation",
        "order" to "order/ownership",
    )
}
