package app.mobiling.client.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.design.MobileDesignDefaults
import app.mobiling.client.design.MobileDesignSystem
import app.mobiling.client.contract.notification.NotificationInboxItem
import kotlinx.coroutines.launch

@Composable
fun NotificationMobileScreen(notificationFeatureBridge: NotificationFeatureBridge?) {
    var items by remember { mutableStateOf<List<NotificationInboxItem>>(emptyList()) }
    var unreadCount by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(notificationFeatureBridge) {
        loading = true
        errorMessage = null
        if (notificationFeatureBridge == null) {
            errorMessage = "Notification service is not available."
            loading = false
            return@LaunchedEffect
        }
        try {
            val payload = notificationFeatureBridge.inbox()
            items = payload.items
            unreadCount = payload.unreadCount
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Notifications are temporarily unavailable."
        } finally {
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        item {
            Text(
                text = if (unreadCount == 1) "1 unread notification" else "$unreadCount unread notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        when {
            loading -> item { Text("Loading notifications…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            errorMessage != null -> item { Text(errorMessage ?: "Notifications are unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items.isEmpty() -> item { Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else -> items(items, key = { it.id }) { item ->
                NotificationRow(item = item, onMarkRead = {
                    if (item.status == "new" && notificationFeatureBridge != null) {
                        scope.launch {
                            try {
                                unreadCount = notificationFeatureBridge.markRead(listOf(item.id))
                                items = items.map { current -> if (current.id == item.id) current.copy(status = "read", readAt = current.readAt ?: "read") else current }
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Notification could not be marked as read."
                            }
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationInboxItem, onMarkRead: () -> Unit) {
    ElevatedCard(
        onClick = onMarkRead,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MobileDesignDefaults.Notification.rowGap),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (item.status == "new") FontWeight.Bold else FontWeight.SemiBold)
                if (item.status == "new") Text("Unread", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (item.body.isNotBlank()) Text(item.body, style = MaterialTheme.typography.bodyMedium)
            Text(item.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
