package app.mobiling.client.contract.vendor.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorDetailPayload(
    val vendorId: String,
    val displayName: String,
    val description: String?,
    val websiteUrl: String?,
    val contactEmail: String?,
    val phoneLabel: String?,
    val cityLabel: String?,
    val ratingLabel: String?,
)
