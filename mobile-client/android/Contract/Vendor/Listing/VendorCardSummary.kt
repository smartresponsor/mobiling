package app.mobiling.client.contract.vendor.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorCardSummary(
    val vendorId: String,
    val displayName: String,
    val categoryLabel: String?,
    val ratingLabel: String?,
    val logoUrl: String?,
    val cityLabel: String?,
)
