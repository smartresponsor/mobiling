package app.mobiling.client.money

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.design.MobileDesignSystem

private data class MoneyDestination(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val route: String?,
    val enabled: Boolean,
)

@Composable
fun MoneyMobileScreen(onRouteSelected: (String) -> Unit) {
    val destinations = listOf(
        MoneyDestination("Cart", "Review items before checkout.", Icons.Default.ShoppingCart, "cart", true),
        MoneyDestination("Wallet", "Balances, reservations and wallet activity.", Icons.Default.Wallet, "wallet", true),
        MoneyDestination("Billing", "Bills, invoices and billing history.", Icons.Default.ReceiptLong, null, false),
        MoneyDestination("Payment", "Payment activity and payment method.", Icons.Default.CreditCard, null, false),
        MoneyDestination("Finance", "Financial summaries and reporting.", Icons.Default.AccountBalance, null, false),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        Text("Money", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Your cart, wallet, billing, payments and financial activity in one place.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        destinations.forEach { destination ->
            ElevatedCard(
                onClick = { destination.route?.let(onRouteSelected) },
                enabled = destination.enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MobileDesignSystem.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.lg),
                ) {
                    Icon(destination.icon, contentDescription = null)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs),
                    ) {
                        Text(destination.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(destination.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!destination.enabled) {
                        AssistChip(onClick = {}, label = { Text("Coming soon") })
                    }
                }
            }
        }
    }
}
