package app.mobiling.client.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.design.MobileDesignSystem
import app.mobiling.client.data.wallet.WalletBalancePayload
import app.mobiling.client.data.wallet.WalletGateway

private data class WalletDestination(val label: String, val description: String, val icon: ImageVector, val route: String)

@Composable
fun WalletMobileScreen(walletGateway: WalletGateway? = null, onRouteSelected: (String) -> Unit) {
    var balance by remember { mutableStateOf<WalletBalancePayload?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(walletGateway) {
        if (walletGateway != null) {
            runCatching { walletGateway.loadBalance() }
                .onSuccess { balance = it; error = null }
                .onFailure { error = it.message ?: "Wallet balance is unavailable." }
        }
    }

    val destinations = listOf(
        WalletDestination("Transaction", "Review wallet ledger activity.", Icons.Default.History, "wallet/transaction"),
        WalletDestination("Funding", "Add or review incoming funds.", Icons.Default.CallReceived, "wallet/funding"),
        WalletDestination("Withdrawal", "Review outgoing wallet funds.", Icons.Default.Payments, "wallet/withdrawal"),
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md)) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
            Column {
                Text("Wallet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("Balance, funding, withdrawals and ledger activity.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg)) {
                val primary = balance?.currency?.firstOrNull()
                Text("Available balance", style = MaterialTheme.typography.labelLarge)
                Text(formatMinor(primary?.availableMinor ?: 0L, primary?.code ?: "USD"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                if (primary != null) {
                    Text("Reserved ${formatMinor(primary.reservedMinor, primary.code)} · Total ${formatMinor(primary.totalMinor, primary.code)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val statusText = error ?: if (balance?.walletId == null) {
                        "Wallet is ready to activate when your first balance is created."
                    } else {
                        "No wallet balance is available yet."
                    }
                    Text(statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        destinations.forEach { destination ->
            ElevatedCard(onClick = { onRouteSelected(destination.route) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.lg),
                ) {
                    Icon(destination.icon, contentDescription = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(destination.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(destination.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun formatMinor(amountMinor: Long, currency: String): String =
    "%s %.2f".format(currency, amountMinor / 100.0)
