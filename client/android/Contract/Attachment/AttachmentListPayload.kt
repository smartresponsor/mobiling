package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentListPayload(
    val ownerType: String,
    val ownerId: String,
    val count: Int,
    val items: List<AttachmentItemPayload>,
    val payloadText: String,
)
