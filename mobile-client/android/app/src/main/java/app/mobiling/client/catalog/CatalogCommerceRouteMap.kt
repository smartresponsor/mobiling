package app.mobiling.client.catalog

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical catalog commerce route map.
 */
class CatalogCommerceRouteMap {
    fun primaryFlows(): List<CatalogCommerceRoute> = listOf(
        CatalogCommerceRoute.VENDOR,
        CatalogCommerceRoute.PRODUCT,
    )
}
