package app.mobiling.client.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import app.mobiling.client.data.wallet.WalletGateway
import app.mobiling.client.data.wallet.WalletOperationItem
import app.mobiling.client.design.MobileDesignSystem
import kotlinx.coroutines.launch

@Composable
fun WalletWithdrawalDetailMobileScreen(withdrawalId: String, walletGateway: WalletGateway?) {
    var withdrawal by remember(withdrawalId) { mutableStateOf<WalletOperationItem?>(null) }
    var error by remember(withdrawalId) { mutableStateOf<String?>(null) }
    var busy by remember(withdrawalId) { mutableStateOf(false) }
    var refreshKey by remember(withdrawalId) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(withdrawalId, walletGateway, refreshKey) {
        if (walletGateway != null) {
            runCatching { walletGateway.loadWithdrawal(withdrawalId) }
                .onSuccess { withdrawal = it; error = null }
                .onFailure { error = it.message ?: "Withdrawal detail is unavailable." }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs)) {
                Text("Withdrawal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("Cash-out lifecycle and wallet reservation state.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val withdrawalItem = withdrawal
        if (withdrawalItem == null) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(error ?: "Loading withdrawal…", modifier = Modifier.padding(MobileDesignSystem.spacing.lg))
                }
            }
        } else {
            val detail = withdrawalItem
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(detail.status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text("${detail.currency} %.2f".format(detail.amountMinor / 100.0), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        detail.destinationReference?.let { Text("Destination: ${withdrawalDisplayReference(it)}", style = MaterialTheme.typography.bodyMedium) }
                        detail.sourceReference?.let { Text("Reservation: ${withdrawalShortReference(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        detail.railReference?.let { Text("Rail: ${withdrawalShortReference(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text("ID ${withdrawalShortReference(detail.id)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
                    ) {
                        Text("Lifecycle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        withdrawalTimeline(detail.status).forEach { step ->
                            Row(horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm)) {
                                Text(if (step.complete) "●" else "○", color = if (step.complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Column {
                                    Text(step.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (step.current) FontWeight.SemiBold else FontWeight.Normal)
                                    Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            if (detail.status == "reserved") {
                item {
                    Button(
                        onClick = {
                            val gateway = walletGateway ?: return@Button
                            busy = true
                            scope.launch {
                                runCatching { gateway.cancelWithdrawal(detail.id) }
                                    .onSuccess { refreshKey += 1 }
                                    .onFailure { error = it.message ?: "Withdrawal cancellation failed." }
                                busy = false
                            }
                        },
                        enabled = !busy,
                    ) { Text(if (busy) "Cancelling…" else "Cancel withdrawal") }
                }
            }
        }
    }
}

private data class WithdrawalTimelineStep(
    val title: String,
    val description: String,
    val complete: Boolean,
    val current: Boolean,
)

private fun withdrawalTimeline(status: String): List<WithdrawalTimelineStep> {
    val order = listOf("pending", "reserved", "processing", "succeeded")
    val terminal = status in setOf("failed", "cancelled", "reversed")
    val reached = order.indexOf(status).takeIf { it >= 0 } ?: if (terminal) 1 else 0
    val steps = mutableListOf(
        WithdrawalTimelineStep("Requested", "Withdrawal request created.", true, status == "pending"),
        WithdrawalTimelineStep("Funds reserved", "Wallet funds are held for cash-out.", reached >= 1 || terminal, status == "reserved"),
    )
    if (status != "cancelled") {
        steps += WithdrawalTimelineStep("Processing", "External payout rail accepts the withdrawal.", reached >= 2 || status in setOf("succeeded", "reversed", "failed"), status == "processing")
    }
    if (status in setOf("succeeded", "reversed")) {
        steps += WithdrawalTimelineStep("Completed", "Funds were finalized from the wallet.", true, status == "succeeded")
    }
    if (terminal) {
        val description = when (status) {
            "cancelled" -> "Reserved wallet funds were released."
            "failed" -> "Processing stopped and reserved funds were released or remain recoverable."
            else -> "A completed withdrawal was reversed."
        }
        steps += WithdrawalTimelineStep(status.replaceFirstChar { it.uppercase() }, description, true, true)
    }
    return steps
}

private fun withdrawalShortReference(reference: String): String =
    if (reference.length <= 14) reference else reference.take(8) + "…" + reference.takeLast(4)

private fun withdrawalDisplayReference(reference: String): String =
    reference.substringAfter(':', reference).replace('-', ' ')
