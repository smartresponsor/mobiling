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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.attachment.AttachmentMobileScreen
import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageSendRequest
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.design.MobileDesignDefaults
import app.mobiling.client.design.MobileDesignSystem
import app.mobiling.client.design.component.CanonicalMessageBubble
import app.mobiling.client.design.component.CanonicalMessageComposer
import app.mobiling.client.design.component.CanonicalStateCard
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun MessageMobileScreen(
    messageFeatureBridge: MessageFeatureBridge?,
    currentUserId: String?,
    vendorId: String?,
    attachmentFeatureBridge: AttachmentFeatureBridge?,
    onOpenTask: () -> Unit,
) {
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
    var attachmentOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (attachmentOpen) {
        AttachmentMobileScreen(
            vendorId = vendorId,
            attachmentFeatureBridge = attachmentFeatureBridge,
            onBack = { attachmentOpen = false },
        )
        return
    }

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
            val loadedMessages = bridge.listItems(thread.threadId)
            messages = loadedMessages
            if (thread.unreadCount > 0) {
                val clearedThread = thread.copy(unreadCount = 0)
                selectedThread = clearedThread
                threads = threads.map { item -> if (item.threadId == thread.threadId) clearedThread else item }
                val latestMessage = loadedMessages.lastOrNull()
                val userId = currentUserId
                if (latestMessage != null && userId != null) {
                    try {
                        bridge.markRead(thread.threadId, userId, latestMessage.messageId)
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (exception: Exception) {
            threadError = exception.message ?: "Conversation is temporarily unavailable."
        } finally {
            loadingMessages = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MobileDesignSystem.spacing.lg,
            top = MobileDesignDefaults.MessageTimeline.topInset,
            end = MobileDesignSystem.spacing.lg,
            bottom = MobileDesignDefaults.MessageTimeline.bottomInset,
        ),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
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
                loading -> item { CanonicalStateCard("Loading conversations…", "We are checking recent task and customer messages.") }
                errorMessage != null -> item { CanonicalStateCard("Messages are temporarily unavailable", errorMessage ?: "Try again in a moment.") }
                threads.isEmpty() -> item { CanonicalStateCard("No conversations yet", "Task and customer conversations will appear here after a customer messages you.") }
                filteredThreads.isEmpty() -> item { CanonicalStateCard("No matching conversations", "Try another customer name or disable the unread filter.") }
                else -> items(filteredThreads, key = { it.threadId }) { item ->
                    MessageThreadRow(item, onClick = { selectedThread = item })
                }
            }
        } else {
            item {
                MessageConversationHeader(
                    thread = thread,
                    onBack = { selectedThread = null },
                    onOpenTask = onOpenTask,
                )
            }
            when {
                loadingMessages -> item { CanonicalStateCard("Loading messages…", "Opening this conversation.") }
                threadError != null && messages.isEmpty() -> item { CanonicalStateCard("Conversation is temporarily unavailable", threadError ?: "Try again in a moment.") }
                messages.isEmpty() -> item { CanonicalStateCard("No messages yet", "Send the first message in this conversation.") }
                else -> items(messages, key = { it.messageId }) { message ->
                    CanonicalMessageBubble(
                        body = message.body,
                        timestamp = friendlyTimestamp(message.sentAtIso8601),
                        ownMessage = currentUserId != null && message.senderId == currentUserId,
                    )
                }
            }
            if (threadError != null && messages.isNotEmpty()) {
                item { Text(threadError ?: "Message could not be sent.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
            item {
                CanonicalMessageComposer(
                    draft = draft,
                    sending = sending,
                    onDraftChange = { draft = it },
                    onClear = { draft = "" },
                    onAttach = { attachmentOpen = true },
                    onSend = {
                        val body = draft.trim()
                        val bridge = messageFeatureBridge
                        val userId = currentUserId
                        if (body.isNotBlank() && bridge != null && userId != null && !sending) {
                            scope.launch {
                                sending = true
                                threadError = null
                                val optimisticId = "local-${System.nanoTime()}"
                                val optimisticMessage = MessageItemPayload(
                                    messageId = optimisticId,
                                    threadId = thread.threadId,
                                    body = body,
                                    senderId = userId,
                                    sentAtIso8601 = Instant.now().toString(),
                                )
                                messages = (messages + optimisticMessage).sortedBy { it.sentAtIso8601 }
                                draft = ""
                                threads = threads.map { item ->
                                    if (item.threadId == thread.threadId) item.copy(lastMessagePreview = body, unreadCount = 0, updatedAtIso8601 = optimisticMessage.sentAtIso8601) else item
                                }
                                selectedThread = selectedThread?.takeIf { it.threadId != thread.threadId }
                                    ?: thread.copy(lastMessagePreview = body, unreadCount = 0, updatedAtIso8601 = optimisticMessage.sentAtIso8601)
                                try {
                                    val sentMessage = bridge.send(MessageSendRequest(thread.threadId, userId, body))
                                    messages = messages.map { item -> if (item.messageId == optimisticId) sentMessage else item }.sortedBy { it.sentAtIso8601 }
                                } catch (exception: Exception) {
                                    messages = messages.filterNot { it.messageId == optimisticId }
                                    if (draft.isBlank()) draft = body
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
        horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
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
            trailingIcon = if (query.isNotBlank()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            } else null,
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
        FilterChip(
            selected = unreadOnly,
            onClick = onToggleUnreadOnly,
            label = { Text("Unread") },
            leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null) },
        )
    }
}

@Composable
private fun MessageThreadRow(thread: MessageThreadSummary, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            MessageAvatar(thread)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs),
            ) {
                Text(
                    thread.subject ?: "Customer conversation",
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
                Text(friendlyTimestamp(thread.updatedAtIso8601), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MessageConversationHeader(thread: MessageThreadSummary, onBack: () -> Unit, onOpenTask: () -> Unit) {
    val subjectParts = thread.subject.orEmpty().split(" — ", limit = 2)
    val customerName = subjectParts.firstOrNull()?.takeIf(String::isNotBlank) ?: "Customer"
    val taskTitle = subjectParts.getOrNull(1)?.takeIf(String::isNotBlank)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back to conversations")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customerName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (taskTitle != null) {
                TextButton(onClick = onOpenTask, contentPadding = PaddingValues(0.dp)) {
                    Text("Task: $taskTitle", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (thread.unreadCount > 0) {
                Text("${thread.unreadCount} unread", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
            .size(MobileDesignDefaults.MessageTimeline.avatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
    }
}

private fun friendlyTimestamp(raw: String): String {
    val value = raw.trim().replace('T', ' ').removeSuffix("Z")
    if (value.length >= 16 && value.getOrNull(4) == '-' && value.getOrNull(7) == '-') {
        return "${value.substring(0, 10)} ${value.substring(11, 16)}"
    }
    return value.take(24)
}
