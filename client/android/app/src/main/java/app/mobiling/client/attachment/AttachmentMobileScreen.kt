package app.mobiling.client.attachment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.attachment.AttachmentItemPayload
import app.mobiling.client.contract.attachment.AttachmentListPayload
import coil.compose.AsyncImage
import app.mobiling.client.contract.attachment.AttachmentUploadHandoffRequest
import kotlinx.coroutines.launch

@Composable
fun AttachmentMobileScreen(
    vendorId: String?,
    attachmentFeatureBridge: AttachmentFeatureBridge? = null,
) {
    var attachmentList by remember { mutableStateOf<AttachmentListPayload?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(AttachmentCategory.All) }
    var selectedItem by remember { mutableStateOf<AttachmentItemPayload?>(null) }
    val scope = rememberCoroutineScope()
    val activeVendorId = vendorId?.trim().orEmpty()

    fun refresh() {
        if (activeVendorId.isBlank()) {
            attachmentList = null
            error = "Attachment require an active vendor session."
            loading = false
            return
        }

        if (attachmentFeatureBridge == null) {
            attachmentList = null
            error = "Attachment bridge is not available."
            loading = false
            return
        }

        scope.launch {
            loading = true
            try {
                attachmentList = attachmentFeatureBridge.list(ownerType = "vendor", ownerId = activeVendorId)
                error = null
            } catch (exception: Exception) {
                attachmentList = null
                error = exception.message ?: "Attachment are unavailable."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(activeVendorId, attachmentFeatureBridge) {
        refresh()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(statusText(loading = loading, error = error, attachmentList = attachmentList))
        val allItems = attachmentList?.items.orEmpty()
        val visibleItems = allItems.filter { category.matches(it) }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AttachmentCategory.entries.forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { category = option },
                    label = { Text(option.label) },
                )
            }
        }

        if (!loading && error == null && visibleItems.isEmpty()) {
            Text("No ${category.label.lowercase()} attachments.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxWidth().height(520.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visibleItems, key = { "${it.attachmentId}:${it.context.orEmpty()}:${it.slot.orEmpty()}:${it.position}" }) { item ->
                    AttachmentBrowserCard(item = item, onClick = { selectedItem = item })
                }
            }
        }
        Button(onClick = { refresh() }) {
            Text("Refresh")
        }
    }

    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            confirmButton = {
                Button(onClick = { selectedItem = null }) { Text("Close") }
            },
            title = { Text(item.displayName()) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.type == "media" && item.mediaKind == "image" && !item.downloadUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.downloadUrl,
                            contentDescription = item.displayName(),
                            modifier = Modifier.fillMaxWidth().height(360.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Text(item.categoryLabel())
                    item.mimeType?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    if (item.size > 0) Text(formatAttachmentSize(item.size), style = MaterialTheme.typography.bodySmall)
                }
            },
        )
    }
}

@Composable
private fun AttachmentBrowserCard(item: AttachmentItemPayload, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(10.dp)) {
            if (item.type == "media" && item.mediaKind == "image" && !item.downloadUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.downloadUrl,
                    contentDescription = item.displayName(),
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = when (item.type) {
                        "document" -> "DOC"
                        else -> item.mediaKind?.uppercase() ?: "FILE"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.height(130.dp).padding(top = 42.dp),
                )
            }
            Text(item.displayName(), style = MaterialTheme.typography.titleSmall, maxLines = 2)
            Text(item.categoryLabel(), style = MaterialTheme.typography.labelSmall)
            if (item.isPrimary) Text("Primary", style = MaterialTheme.typography.labelSmall)
        }
    }
}

private enum class AttachmentCategory(val label: String) {
    All("All"),
    Media("Media"),
    Documents("Documents"),
    Images("Images"),
    Avatars("Avatars"),
    Covers("Covers"),
    OtherImages("Other images"),
    Pdf("PDF"),
    Spreadsheets("Spreadsheets");

    fun matches(item: AttachmentItemPayload): Boolean = when (this) {
        All -> true
        Media -> item.type == "media"
        Documents -> item.type == "document"
        Images -> item.type == "media" && item.mediaKind == "image"
        Avatars -> item.type == "media" && item.mediaKind == "image" && item.context == "profile" && item.slot == "avatar"
        Covers -> item.type == "media" && item.mediaKind == "image" && item.context == "profile" && item.slot == "cover"
        OtherImages -> item.type == "media" && item.mediaKind == "image" && !(item.context == "profile" && item.slot in setOf("avatar", "cover"))
        Pdf -> item.type == "document" && item.documentKind == "pdf"
        Spreadsheets -> item.type == "document" && item.documentKind == "spreadsheet"
    }
}

private fun AttachmentItemPayload.displayName(): String =
    title?.takeIf(String::isNotBlank)
        ?: originalName?.takeIf(String::isNotBlank)
        ?: "Attachment $attachmentId"

private fun AttachmentItemPayload.categoryLabel(): String = when {
    context == "profile" && slot == "avatar" -> "Profile avatar"
    context == "profile" && slot == "cover" -> "Profile cover"
    type == "document" -> documentKind?.replace('_', ' ')?.replaceFirstChar(Char::uppercase) ?: "Document"
    type == "media" -> mediaKind?.replaceFirstChar(Char::uppercase) ?: "Media"
    else -> "Attachment"
}

private fun formatAttachmentSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}

private fun statusText(
    loading: Boolean,
    error: String?,
    attachmentList: AttachmentListPayload?,
): String = when {
    loading -> "Loading attachment..."
    error != null -> error
    attachmentList != null -> "Current attachment status: ${attachmentList.count} item(s)"
    else -> "Current attachment status: not loaded"
}
