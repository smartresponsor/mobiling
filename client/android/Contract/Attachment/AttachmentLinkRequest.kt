package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentLinkRequest(
    val attachmentId: Long,
    val ownerType: String,
    val ownerId: String,
    val context: String? = null,
    val slot: String? = null,
    val position: Int = 0,
    val isPrimary: Boolean = false,
)
