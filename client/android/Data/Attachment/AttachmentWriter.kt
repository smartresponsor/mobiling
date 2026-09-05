package app.mobiling.client.data.attachment

import app.mobiling.client.contract.attachment.AttachmentFileHandoffPayload
import app.mobiling.client.contract.attachment.AttachmentItemPayload
import app.mobiling.client.contract.attachment.AttachmentLinkPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest
import app.mobiling.client.contract.attachment.AttachmentUploadHandoffPayload
import app.mobiling.client.contract.attachment.AttachmentUploadHandoffRequest

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface AttachmentWriter {
    suspend fun attachAttachment(request: AttachmentLinkRequest): AttachmentLinkPayload
    suspend fun fileHandoff(attachmentId: String): AttachmentFileHandoffPayload
    suspend fun uploadHandoff(request: AttachmentUploadHandoffRequest): AttachmentUploadHandoffPayload
    suspend fun uploadAttachment(
        request: AttachmentUploadHandoffRequest,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): AttachmentItemPayload
}
