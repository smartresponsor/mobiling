package app.mobiling.client.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway
import app.mobiling.client.ui.vendor.payout.MobileVendorPayoutScreenContract
import app.mobiling.client.usecase.vendor.payout.LoadVendorPayoutUseCase

@Composable
fun MobileVendorPayoutScreen(vendorId: String?, vendorPayoutGateway: VendorPayoutGateway?) {
    var payout by remember { mutableStateOf<MobileVendorPayoutScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(vendorId, vendorPayoutGateway) {
        payout = null; errorMessage = null
        if (vendorId.isNullOrBlank()) { errorMessage = "Payout requires an active vendor session."; return@LaunchedEffect }
        if (vendorPayoutGateway == null) { errorMessage = "Vendor payout gateway is not available."; return@LaunchedEffect }
        try { payout = MobileVendorPayoutScreenContract.from(LoadVendorPayoutUseCase(vendorPayoutGateway).load(vendorId)) } catch (exception: Exception) { errorMessage = exception.message ?: "Vendor payout is temporarily unavailable." }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Vendor Payout", fontWeight = FontWeight.Bold) }
        when {
            errorMessage != null -> item { Text(errorMessage ?: "Vendor payout is temporarily unavailable.") }
            payout == null -> item { Text("Loading vendor payout...") }
            else -> item { val value = payout ?: return@item; Text("Vendor ID: ${value.vendorId}\nStatus: ${value.payoutStatus ?: "—"}\nAccount: ${value.payoutAccountLabel ?: "—"}\nAvailable: ${amountLabel(value.availableAmount, value.currency)}\nPending: ${amountLabel(value.pendingAmount, value.currency)}") }
        }
    }
}

private fun amountLabel(amount: Double, currency: String?): String = listOfNotNull(currency?.takeIf { it.isNotBlank() }, String.format("%.2f", amount)).joinToString(" ")
