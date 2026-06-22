package app.mobiling.client.catalog

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Catalog-owned commercial discovery routes.
 *
 * Catalog stays a Dashboard primary section, but its navigation semantics
 * explicitly point into Vendor-owned commerce flows instead of pretending
 * Product is a flat sibling of Catalog.
 */
enum class CatalogCommerceRoute {
    VENDOR,
    PRODUCT,
}
