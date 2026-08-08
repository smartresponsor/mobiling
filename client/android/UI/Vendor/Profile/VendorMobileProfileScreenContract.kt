package app.mobiling.client.ui.vendor.profile

import app.mobiling.client.contract.vendor.profile.VendorMobileProfilePayload

data class VendorMobileProfileScreenContract(
    val vendorId: String,
    val title: String,
    val brandName: String?,
    val status: String?,
    val completionPercent: Int,
    val readyForPublishing: Boolean,
    val nextAction: String?,
    val about: String?,
    val website: String?,
    val avatarUrl: String?,
    val avatarAttachmentId: String?,
    val coverUrl: String?,
    val coverAttachmentId: String?,
    val canEditProfileMedia: Boolean,
    val publicationStatus: String?,
) {
    companion object {
        fun from(payload: VendorMobileProfilePayload): VendorMobileProfileScreenContract =
            VendorMobileProfileScreenContract(
                vendorId = payload.vendorId,
                title = payload.displayName ?: payload.brandName ?: "My Profile",
                brandName = payload.brandName,
                status = payload.status,
                completionPercent = payload.completionPercent,
                readyForPublishing = payload.readyForPublishing,
                nextAction = payload.nextAction,
                about = payload.about,
                website = payload.website,
                avatarUrl = payload.avatarUrl,
                avatarAttachmentId = payload.avatarAttachmentId,
                coverUrl = payload.coverUrl,
                coverAttachmentId = payload.coverAttachmentId,
                canEditProfileMedia = payload.canEditProfileMedia,
                publicationStatus = payload.publicationStatus,
            )
    }
}
