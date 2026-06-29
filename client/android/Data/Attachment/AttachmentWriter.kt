package app.mobiling.client.data.attachment

import app.mobiling.client.contract.attachment.AttachmentLinkPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface AttachmentWriter {
    suspend fun attachAttachment(request: AttachmentLinkRequest): AttachmentLinkPayload
}
