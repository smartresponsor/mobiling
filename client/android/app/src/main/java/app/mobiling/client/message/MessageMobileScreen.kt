package app.mobiling.client.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.message.thread.MessageThreadSummary

@Composable
fun MessageMobileScreen(messageFeatureBridge: MessageFeatureBridge?) {
    var threads by remember { mutableStateOf<List<MessageThreadSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messageFeatureBridge) {
        loading = true
        errorMessage = null
        threads = emptyList()

        if (messageFeatureBridge == null) {
            errorMessage = "Messaging gateway is not available."
            loading = false
            return@LaunchedEffect
        }

        try {
            threads = messageFeatureBridge.listThreads()
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Messages are temporarily unavailable."
        } finally {
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Messages", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        when {
            loading -> item { MessageStateText("Loading conversations…") }
            errorMessage != null -> item { MessageStateText(errorMessage ?: "Messages are temporarily unavailable.") }
            threads.isEmpty() -> item { MessageStateText("No task or customer conversations yet.") }
            else -> items(threads, key = { it.threadId }) { thread -> MessageThreadCard(thread) }
        }
    }
}

@Composable
private fun MessageThreadCard(thread: MessageThreadSummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                thread.subject ?: "Conversation ${thread.threadId.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(thread.lastMessagePreview, style = MaterialTheme.typography.bodyMedium)
            if (thread.updatedAtIso8601.isNotBlank()) {
                Text(thread.updatedAtIso8601, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (thread.unreadCount > 0) {
                Text("${thread.unreadCount} unread", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MessageStateText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

