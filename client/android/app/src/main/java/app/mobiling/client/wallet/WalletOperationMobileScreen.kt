package app.mobiling.client.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import app.mobiling.client.data.wallet.WalletWithdrawalDestination
import app.mobiling.client.design.MobileDesignSystem
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun WalletOperationMobileScreen(type: String, walletGateway: WalletGateway?, onRouteSelected: (String) -> Unit = {}) {
    var operation by remember(type) { mutableStateOf<List<WalletOperationItem>>(emptyList()) }
    var error by remember(type) { mutableStateOf<String?>(null) }
    var destination by remember(type) { mutableStateOf<List<WalletWithdrawalDestination>>(emptyList()) }
    var selectedDestinationId by remember(type) { mutableStateOf<String?>(null) }
    var destinationMenuOpen by remember(type) { mutableStateOf(false) }
    var amountText by remember(type) { mutableStateOf("") }
    var actionMessage by remember(type) { mutableStateOf<String?>(null) }
    var actionBusy by remember(type) { mutableStateOf(false) }
    var refreshKey by remember(type) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(type, walletGateway, refreshKey) {
        if (walletGateway != null) {
            runCatching {
                if (type == "funding") walletGateway.loadFunding() else walletGateway.loadWithdrawal()
            }.onSuccess {
                operation = it.item
                error = null
            }.onFailure {
                error = it.message ?: "Wallet $type is unavailable."
            }
            if (type == "withdrawal") {
                runCatching { walletGateway.loadWithdrawalDestination() }
                    .onSuccess {
                        destination = it.item
                        if (selectedDestinationId !in it.item.map(WalletWithdrawalDestination::id)) {
                            selectedDestinationId = it.item.firstOrNull()?.id
                        }
                    }
                    .onFailure { actionMessage = it.message ?: "Withdrawal destination is unavailable." }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        item {
            val title = if (type == "funding") "Funding" else "Withdrawal"
            val description = if (type == "funding") {
                "Incoming wallet funding activity and its current status."
            } else {
                "Outgoing cash-out activity, destination and processing status."
            }
            Column(verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (type == "withdrawal") {
            item {
                val selectedDestination = destination.firstOrNull { it.id == selectedDestinationId } ?: destination.firstOrNull()
                val amountMinor = parseAmountMinor(amountText)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
                    ) {
                        Text("New withdrawal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (selectedDestination == null) {
                            Text("No active withdrawal destination is available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column {
                                Button(onClick = { destinationMenuOpen = true }, enabled = !actionBusy) {
                                    Text("Destination: ${selectedDestination.label}")
                                }
                                DropdownMenu(expanded = destinationMenuOpen, onDismissRequest = { destinationMenuOpen = false }) {
                                    destination.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                selectedDestinationId = option.id
                                                destinationMenuOpen = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { value -> amountText = value.filter { it.isDigit() || it == '.' }.take(12) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Amount (USD)") },
                            singleLine = true,
                            enabled = !actionBusy,
                        )
                        Button(
                            onClick = {
                                val gateway = walletGateway ?: return@Button
                                val target = selectedDestination ?: return@Button
                                val minor = amountMinor ?: return@Button
                                actionBusy = true
                                actionMessage = null
                                scope.launch {
                                    runCatching {
                                        gateway.requestWithdrawal(minor, "USD", target.id, UUID.randomUUID().toString())
                                    }.onSuccess {
                                        amountText = ""
                                        actionMessage = "Withdrawal reserved. External payout processing is not enabled yet."
                                        refreshKey += 1
                                    }.onFailure {
                                        actionMessage = it.message ?: "Withdrawal request failed."
                                    }
                                    actionBusy = false
                                }
                            },
                            enabled = !actionBusy && selectedDestination != null && amountMinor != null && amountMinor > 0,
                        ) {
                            Text(if (actionBusy) "Working…" else "Request withdrawal")
                        }
                        actionMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
        if (operation.isEmpty()) {
            item {
                val emptyText = error ?: if (type == "funding") "No funding activity yet." else "No withdrawals yet."
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(emptyText, modifier = Modifier.padding(MobileDesignSystem.spacing.lg), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(operation, key = { it.id }) { item ->
            val cardModifier = if (type == "withdrawal") {
                Modifier.fillMaxWidth().clickable { onRouteSelected("wallet/withdrawal/${item.id}") }
            } else {
                Modifier.fillMaxWidth()
            }
            ElevatedCard(modifier = cardModifier) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs)) {
                        Text(item.status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (type == "withdrawal") {
                            item.sourceType?.let { sourceType ->
                                Text("Source: ${sourceType.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.destinationReference?.let { destination ->
                                Text("Destination: ${displayReference(destination)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.sourceReference?.let { sourceReference ->
                                Text("Reservation: ${shortReference(sourceReference)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.railReference?.let { railReference ->
                                Text("Rail: ${shortReference(railReference)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("ID ${shortReference(item.id)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (type == "withdrawal" && item.status == "reserved") {
                            Button(
                                onClick = {
                                    val gateway = walletGateway ?: return@Button
                                    actionBusy = true
                                    actionMessage = null
                                    scope.launch {
                                        runCatching { gateway.cancelWithdrawal(item.id) }
                                            .onSuccess {
                                                actionMessage = "Withdrawal cancelled and reserved funds released."
                                                refreshKey += 1
                                            }
                                            .onFailure { actionMessage = it.message ?: "Withdrawal cancellation failed." }
                                        actionBusy = false
                                    }
                                },
                                enabled = !actionBusy,
                            ) {
                                Text("Cancel")
                            }
                        }
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

private fun shortReference(reference: String): String =
    if (reference.length <= 14) reference else reference.take(8) + "…" + reference.takeLast(4)

private fun displayReference(reference: String): String =
    reference.substringAfter(':', reference).replace('-', ' ')

private fun parseAmountMinor(value: String): Long? = runCatching {
    val amount = BigDecimal(value.trim()).setScale(2, RoundingMode.UNNECESSARY)
    amount.movePointRight(2).longValueExact().takeIf { it > 0 }
}.getOrNull()
