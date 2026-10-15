package app.mobiling.client.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.design.MobileDesignDefaults
import app.mobiling.client.design.MobileDesignSystem

@Composable
fun CanonicalMessageBubble(
    body: String,
    timestamp: String,
    ownMessage: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = MobileDesignSystem.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (ownMessage) Arrangement.End else Arrangement.Start,
    ) {
        ElevatedCard(
            modifier = Modifier.widthIn(max = MobileDesignDefaults.MessageBubble.maxWidth),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (ownMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = MobileDesignDefaults.MessageBubble.horizontalInset,
                    vertical = MobileDesignDefaults.MessageBubble.verticalInset,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(body, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun CanonicalStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val spacing = MobileDesignSystem.spacing
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.xl, vertical = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
