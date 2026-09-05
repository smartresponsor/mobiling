package app.mobiling.client.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import app.mobiling.client.data.wallet.WalletGateway
import app.mobiling.client.data.wallet.WalletTransactionItem
import app.mobiling.client.design.MobileDesignSystem

@Composable
fun WalletTransactionMobileScreen(walletGateway: WalletGateway?) {
    var transaction by remember { mutableStateOf<List<WalletTransactionItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(walletGateway) {
        if (walletGateway != null) {
            runCatching { walletGateway.loadTransaction() }
                .onSuccess { transaction = it.item; error = null }
                .onFailure { error = it.message ?: "Wallet transaction is unavailable." }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs)) {
                Text("Transaction", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("Wallet ledger activity appears here as funds move in or out.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (transaction.isEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(error ?: "No wallet transactions yet.", modifier = Modifier.padding(MobileDesignSystem.spacing.lg), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(transaction, key = { it.transactionId }) { item ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(item.postedAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "%s %.2f".format(item.currency, item.amountMinor / 100.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
