package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentUploadHandoffPayload(
    val uploadUrl: String,
    val method: String,
    val fieldName: String,
    val handoffMode: String,
    val payloadText: String,
)
