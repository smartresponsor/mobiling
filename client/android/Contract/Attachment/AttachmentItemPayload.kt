package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentItemPayload(
    val attachmentId: String,
    val type: String,
    val mediaKind: String? = null,
    val documentKind: String? = null,
    val originalName: String? = null,
    val title: String? = null,
    val mimeType: String? = null,
    val extension: String? = null,
    val size: Long = 0L,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Int? = null,
    val pageCount: Int? = null,
    val context: String? = null,
    val slot: String? = null,
    val isPrimary: Boolean = false,
    val position: Int = 0,
    val createdAt: String? = null,
    val downloadUrl: String? = null,
    val payloadText: String = "",
)
