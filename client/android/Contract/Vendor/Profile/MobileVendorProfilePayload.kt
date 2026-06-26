package app.mobiling.client.contract.vendor.profile

data class MobileVendorProfilePayload(
    val vendorId: String,
    val displayName: String?,
    val brandName: String?,
    val status: String?,
    val completionPercent: Int,
    val readyForPublishing: Boolean,
    val nextAction: String?,
    val avatarUrl: String?,
    val coverUrl: String?,
    val about: String?,
    val website: String?,
    val publicationStatus: String?,
)
