package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentFileHandoffPayload(
    val attachmentId: String,
    val downloadUrl: String,
    val mimeType: String?,
    val fileName: String?,
    val handoffMode: String,
    val payloadText: String,
)
