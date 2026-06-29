package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentItemPayload(
    val attachmentId: String,
    val type: String,
    val mimeType: String?,
    val downloadUrl: String?,
    val payloadText: String,
)
