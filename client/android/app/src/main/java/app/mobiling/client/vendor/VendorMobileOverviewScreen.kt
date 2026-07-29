package app.mobiling.client.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import app.mobiling.client.ui.vendor.summary.VendorMobileSummaryScreenContract
import app.mobiling.client.usecase.vendor.summary.VendorLoadSummaryUseCase

/**
 * Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp.
 *
 * Product-facing vendor landing screen backed by the canonical summary gateway.
 */
@Composable
fun VendorMobileOverviewScreen(
    vendorId: String?,
    vendorSummaryGateway: VendorSummaryGateway?,
    onRouteSelected: (String) -> Unit,
) {
    var summary by remember { mutableStateOf<VendorMobileSummaryScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vendorId, vendorSummaryGateway) {
        summary = null
        errorMessage = null

        if (vendorId.isNullOrBlank()) {
            errorMessage = "Vendor overview requires an active vendor session."
            return@LaunchedEffect
        }

        if (vendorSummaryGateway == null) {
            errorMessage = "Vendor overview is available after the summary service is connected."
            return@LaunchedEffect
        }

        try {
            summary = VendorMobileSummaryScreenContract.from(
                VendorLoadSummaryUseCase(vendorSummaryGateway).load(vendorId),
            )
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Vendor overview is temporarily unavailable."
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("My Vendor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Business status, profile readiness, payments, and activity in one place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            summary != null -> item {
                val currentSummary = summary ?: return@item
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            currentSummary.brandName?.takeIf { it.isNotBlank() } ?: "Vendor workspace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            currentSummary.status?.takeIf { it.isNotBlank() } ?: "Status unavailable",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text("Profile completion: ${currentSummary.profileCompletionPercent}%")
                        LinearProgressIndicator(
                            progress = currentSummary.profileCompletionPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        currentSummary.nextAction?.takeIf { it.isNotBlank() }?.let { nextAction ->
                            Text(
                                "Next: $nextAction",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            errorMessage != null -> item {
                ElevatedCard {
                    Text(
                        errorMessage ?: "Vendor overview is temporarily unavailable.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> item { Text("Loading vendor overview...") }
        }

        item {
            Text("Workspace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                VendorOverviewAction("My Profile", "Complete and review your public vendor identity.", Icons.Default.Person, "vendor/profile", onRouteSelected)
                VendorOverviewAction("Summary", "Open the detailed vendor status and readiness view.", Icons.Default.Dashboard, "vendor/summary", onRouteSelected)
                VendorOverviewAction("Statement", "Review vendor statement totals and status.", Icons.Default.ReceiptLong, "vendor/statement", onRouteSelected)
                VendorOverviewAction("Payout", "Review available and pending payout amounts.", Icons.Default.Payments, "vendor/payout", onRouteSelected)
                VendorOverviewAction("Transaction", "Review recent vendor transaction activity.", Icons.Default.ReceiptLong, "vendor/transaction", onRouteSelected)
                VendorOverviewAction("My Attachment", "Manage files linked to this vendor workspace.", Icons.Default.AttachFile, "vendor/attachment", onRouteSelected)
                VendorOverviewAction("Products", "Manage vendor product listings.", Icons.Default.Inventory2, "vendor/product", onRouteSelected)
                VendorOverviewAction("Orders", "Review customer orders, shipments, and tax.", Icons.Default.ReceiptLong, "vendor/order", onRouteSelected)
                VendorOverviewAction("Projects", "Review active and completed vendor projects.", Icons.Default.Work, "vendor/project", onRouteSelected)
            }
        }
    }
}

@Composable
private fun VendorOverviewAction(
    title: String,
    description: String,
    icon: ImageVector,
    route: String,
    onRouteSelected: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onRouteSelected(route) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
