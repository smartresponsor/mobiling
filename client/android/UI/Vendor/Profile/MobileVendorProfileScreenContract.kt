package app.mobiling.client.ui.vendor.profile

import app.mobiling.client.contract.vendor.profile.MobileVendorProfilePayload

data class MobileVendorProfileScreenContract(
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
    val coverUrl: String?,
    val publicationStatus: String?,
) {
    companion object {
        fun from(payload: MobileVendorProfilePayload): MobileVendorProfileScreenContract =
            MobileVendorProfileScreenContract(
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
                coverUrl = payload.coverUrl,
                publicationStatus = payload.publicationStatus,
            )
    }
}
