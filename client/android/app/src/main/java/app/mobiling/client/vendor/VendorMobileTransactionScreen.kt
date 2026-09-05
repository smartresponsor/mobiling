package app.mobiling.client.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.data.vendor.transaction.VendorTransactionGateway
import app.mobiling.client.ui.vendor.transaction.VendorMobileTransactionScreenContract
import app.mobiling.client.usecase.vendor.transaction.VendorLoadTransactionUseCase

@Composable
fun VendorMobileTransactionScreen(vendorId: String?, vendorTransactionGateway: VendorTransactionGateway?) {
    var transaction by remember { mutableStateOf<VendorMobileTransactionScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(vendorId, vendorTransactionGateway) {
        transaction = null; errorMessage = null
        if (vendorId.isNullOrBlank()) { errorMessage = "Transaction requires an active vendor session."; return@LaunchedEffect }
        if (vendorTransactionGateway == null) { errorMessage = "Vendor transaction gateway is not available."; return@LaunchedEffect }
        try { transaction = VendorMobileTransactionScreenContract.from(VendorLoadTransactionUseCase(vendorTransactionGateway).load(vendorId)) } catch (exception: Exception) { errorMessage = exception.message ?: "Vendor transaction is temporarily unavailable." }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            errorMessage != null -> item { Text(errorMessage ?: "Vendor transaction is temporarily unavailable.") }
            transaction == null -> item { Text("Loading vendor transaction...") }
            transaction?.transactions?.isEmpty() == true -> item { Text("No vendor transactions yet.") }
            else -> items(transaction?.transactions ?: emptyList()) { item -> Text("${item.type ?: "Transaction"} · ${item.status ?: "—"}\n${amountLabel(item.amount, item.currency)} · ${item.createdAt ?: "—"}\n${item.id ?: "—"}") }
        }
    }
}

private fun amountLabel(amount: Double, currency: String?): String = listOfNotNull(currency?.takeIf { it.isNotBlank() }, String.format("%.2f", amount)).joinToString(" ")
