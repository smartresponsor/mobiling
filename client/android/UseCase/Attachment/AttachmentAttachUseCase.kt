package app.mobiling.client.usecase.attachment

import app.mobiling.client.contract.attachment.AttachmentLinkPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest
import app.mobiling.client.data.attachment.AttachmentWriter

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentAttachUseCase(private val writer: AttachmentWriter) {
    suspend operator fun invoke(request: AttachmentLinkRequest): AttachmentLinkPayload =
        writer.attachAttachment(request)
}
