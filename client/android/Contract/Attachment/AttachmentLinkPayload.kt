package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentLinkPayload(
    val linkId: String,
    val attachmentId: String,
    val ownerType: String,
    val ownerId: String,
    val context: String?,
    val slot: String?,
    val position: Int,
    val isPrimary: Boolean,
    val payloadText: String,
)
