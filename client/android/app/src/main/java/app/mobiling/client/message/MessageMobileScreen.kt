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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageSendRequest
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import kotlinx.coroutines.launch

@Composable
fun MessageMobileScreen(messageFeatureBridge: MessageFeatureBridge?) {
    var threads by remember { mutableStateOf<List<MessageThreadSummary>>(emptyList()) }
    var selectedThread by remember { mutableStateOf<MessageThreadSummary?>(null) }
    var messages by remember { mutableStateOf<List<MessageItemPayload>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var unreadOnly by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var loadingMessages by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var threadError by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredThreads by remember(threads, query, unreadOnly) {
        derivedStateOf {
            val normalizedQuery = query.trim()
            threads.filter { thread ->
                val matchesUnread = !unreadOnly || thread.unreadCount > 0
                val matchesQuery = normalizedQuery.isBlank() || sequenceOf(
                    thread.subject.orEmpty(),
                    thread.lastMessagePreview,
                    thread.updatedAtIso8601,
                ).any { value -> value.contains(normalizedQuery, ignoreCase = true) }
                matchesUnread && matchesQuery
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

    LaunchedEffect(selectedThread?.threadId, messageFeatureBridge) {
        val thread = selectedThread ?: return@LaunchedEffect
        val bridge = messageFeatureBridge ?: return@LaunchedEffect
        loadingMessages = true
        threadError = null
        messages = emptyList()
        try {
            messages = bridge.listItems(thread.threadId)
        } catch (exception: Exception) {
            threadError = exception.message ?: "Conversation is temporarily unavailable."
        } finally {
            loadingMessages = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val thread = selectedThread
        if (thread == null) {
            item {
                MessageSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    unreadOnly = unreadOnly,
                    onToggleUnreadOnly = { unreadOnly = !unreadOnly },
                )
            }

            when {
                loading -> item { MessageStateCard("Loading conversations…", "We are checking recent task and customer messages.") }
                errorMessage != null -> item { MessageStateCard("Messages are temporarily unavailable", errorMessage ?: "Try again in a moment.") }
                threads.isEmpty() -> item { MessageStateCard("No conversations yet", "Task and customer conversations will appear here after a customer messages you.") }
                filteredThreads.isEmpty() -> item { MessageStateCard("No matching conversations", "Try another customer name or disable the unread filter.") }
                else -> items(filteredThreads, key = { it.threadId }) { item ->
                    MessageThreadRow(item, onClick = { selectedThread = item })
                }
            }
        } else {
            item { MessageConversationHeader(thread = thread, onBack = { selectedThread = null }) }
            when {
                loadingMessages -> item { MessageStateCard("Loading messages…", "Opening this conversation.") }
                threadError != null -> item { MessageStateCard("Conversation is temporarily unavailable", threadError ?: "Try again in a moment.") }
                messages.isEmpty() -> item { MessageStateCard("No messages yet", "Send the first message in this conversation.") }
                else -> items(messages, key = { it.messageId }) { message -> MessageBubble(message) }
            }
            item {
                MessageComposer(
                    draft = draft,
                    sending = sending,
                    onDraftChange = { draft = it },
                    onSend = {
                        val body = draft.trim()
                        val bridge = messageFeatureBridge
                        if (body.isNotBlank() && bridge != null && !sending) {
                            scope.launch {
                                sending = true
                                threadError = null
                                try {
                                    bridge.send(MessageSendRequest(thread.threadId, body))
                                    draft = ""
                                    messages = bridge.listItems(thread.threadId)
                                    threads = bridge.listThreads()
                                } catch (exception: Exception) {
                                    threadError = exception.message ?: "Message could not be sent."
                                } finally {
                                    sending = false
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MessageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    unreadOnly: Boolean,
    onToggleUnreadOnly: () -> Unit,
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
            shape = MaterialTheme.shapes.extraLarge,
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
            onClick = onToggleUnreadOnly,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = "Filter conversations",
                tint = if (unreadOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageThreadRow(thread: MessageThreadSummary, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
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
private fun MessageConversationHeader(thread: MessageThreadSummary, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back to conversations")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.subject ?: "Conversation ${thread.threadId.take(8)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (thread.unreadCount > 0) {
                Text("${thread.unreadCount} unread", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageItemPayload) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(message.body, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${message.senderId}  ${message.sentAtIso8601}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageComposer(
    draft: String,
    sending: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = draft,
            onValueChange = onDraftChange,
            enabled = !sending,
            placeholder = { Text("Message") },
            maxLines = 5,
            shape = MaterialTheme.shapes.extraLarge,
        )
        IconButton(
            onClick = onSend,
            enabled = draft.isNotBlank() && !sending,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.Outlined.Send, contentDescription = "Send message", tint = MaterialTheme.colorScheme.onPrimary)
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
