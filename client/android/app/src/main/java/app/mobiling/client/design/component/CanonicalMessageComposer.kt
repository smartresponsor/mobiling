package app.mobiling.client.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import app.mobiling.client.design.MobileDesignSystem

@Composable
fun CanonicalMessageComposer(
    draft: String,
    sending: Boolean,
    onDraftChange: (String) -> Unit,
    onClear: () -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = MobileDesignSystem.messageComposer

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.outerGap),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp && event.key == Key.Enter && draft.isNotBlank() && !sending) {
                        onSend()
                        true
                    } else {
                        false
                    }
                },
            value = draft,
            onValueChange = onDraftChange,
            enabled = !sending,
            placeholder = { Text("Message") },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (draft.isNotBlank() && !sending) onSend() }),
            leadingIcon = {
                IconButton(
                    onClick = onAttach,
                    modifier = Modifier
                        .padding(start = metrics.innerInset)
                        .size(metrics.actionSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add attachment")
                }
            },
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(end = metrics.innerInset),
                    horizontalArrangement = Arrangement.spacedBy(metrics.actionGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (draft.isNotBlank()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier
                                .size(metrics.clearSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear message")
                        }
                    }
                    IconButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .size(metrics.actionSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Icon(Icons.Outlined.MicNone, contentDescription = "AI voice, coming soon")
                    }
                }
            },
        )
        IconButton(
            onClick = onSend,
            enabled = draft.isNotBlank() && !sending,
            modifier = Modifier
                .size(metrics.sendSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send message", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
