package app.mobiling.client.attachment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.attachment.AttachmentListPayload
import kotlinx.coroutines.launch

@Composable
fun AttachmentMobileScreen(
    vendorId: String?,
    attachmentFeatureBridge: AttachmentFeatureBridge? = null,
) {
    var attachmentList by remember { mutableStateOf<AttachmentListPayload?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
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
        Text("Attachment", fontWeight = FontWeight.Bold)
        Text(statusText(loading = loading, error = error, attachmentList = attachmentList))
        attachmentList?.items.orEmpty().forEach { item ->
            ListItem(
                headlineContent = { Text("${item.type}: ${item.attachmentId}") },
                supportingContent = {
                    Text(
                        listOfNotNull(item.mimeType, item.downloadUrl, item.payloadText.takeIf { it.isNotBlank() })
                            .joinToString("\n")
                            .ifBlank { "No attachment metadata." },
                    )
                },
            )
        }
        Button(onClick = { refresh() }) {
            Text("Refresh")
        }
    }
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
