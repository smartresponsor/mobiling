package app.mobiling.client.attachment

import app.mobiling.client.contract.attachment.AttachmentLinkPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest
import app.mobiling.client.contract.attachment.AttachmentListPayload
import app.mobiling.client.data.attachment.AttachmentReader
import app.mobiling.client.data.attachment.AttachmentWriter
import app.mobiling.client.usecase.attachment.AttachmentAttachUseCase
import app.mobiling.client.usecase.attachment.AttachmentLoadListUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentFeatureBridge(
    private val reader: AttachmentReader,
    private val writer: AttachmentWriter,
) {
    suspend fun list(
        ownerType: String,
        ownerId: String,
        context: String? = null,
        slot: String? = null,
    ): AttachmentListPayload =
        AttachmentLoadListUseCase(reader).invoke(ownerType, ownerId, context, slot)

    suspend fun attach(request: AttachmentLinkRequest): AttachmentLinkPayload =
        AttachmentAttachUseCase(writer).invoke(request)
}
