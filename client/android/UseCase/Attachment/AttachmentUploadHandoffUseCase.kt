package app.mobiling.client.usecase.attachment

import app.mobiling.client.contract.attachment.AttachmentUploadHandoffPayload
import app.mobiling.client.contract.attachment.AttachmentUploadHandoffRequest
import app.mobiling.client.data.attachment.AttachmentWriter

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentUploadHandoffUseCase(private val writer: AttachmentWriter) {
    suspend operator fun invoke(request: AttachmentUploadHandoffRequest): AttachmentUploadHandoffPayload =
        writer.uploadHandoff(request)
}
