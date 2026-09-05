package app.mobiling.client.contract.vendor.profile

data class VendorMobileProfilePayload(
    val vendorId: String,
    val displayName: String?,
    val brandName: String?,
    val status: String?,
    val completionPercent: Int,
    val readyForPublishing: Boolean,
    val nextAction: String?,
    val avatarUrl: String?,
    val avatarAttachmentId: String?,
    val coverUrl: String?,
    val coverAttachmentId: String?,
    val canEditProfileMedia: Boolean,
    val about: String?,
    val website: String?,
    val publicationStatus: String?,
)
