package app.mobiling.client.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.mobiling.client.design.OneTaskerDesignTokens
import app.mobiling.client.navigation.CanonicalTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccessFlowShell(
    title: String,
    subtitle: String,
    styledSubtitle: AnnotatedString? = null,
    primaryActionLabel: String,
    primaryActionLoading: Boolean = false,
    secondaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    onBack: () -> Unit,
    status: String?,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CanonicalTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { shellPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(shellPadding)
                .verticalScroll(rememberScrollState())
                .padding(OneTaskerDesignTokens.Spacing.Xxl),
            verticalArrangement = Arrangement.spacedBy(OneTaskerDesignTokens.Spacing.Lg),
        ) {
            Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(OneTaskerDesignTokens.Spacing.Lg),
                    verticalArrangement = Arrangement.spacedBy(OneTaskerDesignTokens.Spacing.Md),
                ) {
                    Text(
                        text = styledSubtitle ?: AnnotatedString(subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    content()
                }
            }

            if (status != null) {
                Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth(),
                enabled = !primaryActionLoading,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (primaryActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Text(if (primaryActionLoading) "Signing in…" else primaryActionLabel)
                }
            }
            OutlinedButton(onClick = onSecondaryAction, modifier = Modifier.fillMaxWidth()) {
                Text(secondaryActionLabel)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Return to access welcome")
            }
            GuestLegalFooter()
        }
    }
}
