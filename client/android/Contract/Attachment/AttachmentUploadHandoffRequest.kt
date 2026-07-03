package app.mobiling.client.contract.attachment

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class AttachmentUploadHandoffRequest(
    val ownerType: String,
    val ownerId: String,
    val context: String? = null,
    val slot: String? = null,
    val isPrimary: Boolean = false,
    val title: String? = null,
    val description: String? = null,
    val altText: String? = null,
)
