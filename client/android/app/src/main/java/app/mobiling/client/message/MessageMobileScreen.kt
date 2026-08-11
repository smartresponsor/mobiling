package app.mobiling.client.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.message.thread.MessageThreadSummary

@Composable
fun MessageMobileScreen(messageFeatureBridge: MessageFeatureBridge?) {
    var threads by remember { mutableStateOf<List<MessageThreadSummary>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val filteredThreads by remember(threads, query) {
        derivedStateOf {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isBlank()) {
                threads
            } else {
                threads.filter { thread ->
                    sequenceOf(
                        thread.subject.orEmpty(),
                        thread.lastMessagePreview,
                        thread.updatedAtIso8601,
                    ).any { value -> value.contains(normalizedQuery, ignoreCase = true) }
                }
            }
        }
    }

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
        contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MessageSearchBar(
                query = query,
                onQueryChange = { query = it },
            )
        }

        when {
            loading -> item { MessageStateCard("Loading conversations…", "We are checking recent task and customer messages.") }
            errorMessage != null -> item { MessageStateCard("Messages are temporarily unavailable", errorMessage ?: "Try again in a moment.") }
            threads.isEmpty() -> item { MessageStateCard("No conversations yet", "Task and customer conversations will appear here after a customer messages you.") }
            filteredThreads.isEmpty() -> item { MessageStateCard("No matching conversations", "Try another customer name, service, city, or message keyword.") }
            else -> items(filteredThreads, key = { it.threadId }) { thread -> MessageThreadRow(thread) }
        }
    }
}

@Composable
private fun MessageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            placeholder = {
                Text("Search by customer name")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = "Filter conversations",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageThreadRow(thread: MessageThreadSummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MessageAvatar(thread)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    thread.subject ?: "Conversation ${thread.threadId.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    thread.lastMessagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (thread.unreadCount > 0) {
                    Text("${thread.unreadCount} unread", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (thread.updatedAtIso8601.isNotBlank()) {
                Text(thread.updatedAtIso8601, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MessageAvatar(thread: MessageThreadSummary) {
    val initials = (thread.subject ?: thread.threadId)
        .split(' ', '-', '_')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "M" }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MessageStateCard(title: String, description: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
