package app.mobiling.client.usecase.attachment

import app.mobiling.client.contract.attachment.AttachmentListPayload
import app.mobiling.client.data.attachment.AttachmentReader

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentLoadListUseCase(private val reader: AttachmentReader) {
    suspend operator fun invoke(
        ownerType: String,
        ownerId: String,
        context: String? = null,
        slot: String? = null,
    ): AttachmentListPayload =
        reader.listAttachment(ownerType = ownerType, ownerId = ownerId, context = context, slot = slot)
}
