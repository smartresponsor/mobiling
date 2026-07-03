package app.mobiling.client.usecase.attachment

import app.mobiling.client.contract.attachment.AttachmentFileHandoffPayload
import app.mobiling.client.data.attachment.AttachmentWriter

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentFileHandoffUseCase(private val writer: AttachmentWriter) {
    suspend operator fun invoke(attachmentId: String): AttachmentFileHandoffPayload = writer.fileHandoff(attachmentId)
}
