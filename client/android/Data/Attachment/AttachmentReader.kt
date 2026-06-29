package app.mobiling.client.data.attachment

import app.mobiling.client.contract.attachment.AttachmentListPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface AttachmentReader {
    suspend fun listAttachment(
        ownerType: String,
        ownerId: String,
        context: String? = null,
        slot: String? = null,
    ): AttachmentListPayload
}
