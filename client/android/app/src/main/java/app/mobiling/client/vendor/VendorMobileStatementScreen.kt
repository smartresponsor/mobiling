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
import app.mobiling.client.data.vendor.statement.VendorStatementGateway
import app.mobiling.client.ui.vendor.statement.VendorMobileStatementScreenContract
import app.mobiling.client.usecase.vendor.statement.VendorLoadStatementUseCase

@Composable
fun VendorMobileStatementScreen(vendorId: String?, vendorStatementGateway: VendorStatementGateway?) {
    var statement by remember { mutableStateOf<VendorMobileStatementScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(vendorId, vendorStatementGateway) {
        statement = null; errorMessage = null
        if (vendorId.isNullOrBlank()) { errorMessage = "Statement requires an active vendor session."; return@LaunchedEffect }
        if (vendorStatementGateway == null) { errorMessage = "Vendor statement gateway is not available."; return@LaunchedEffect }
        try { statement = VendorMobileStatementScreenContract.from(VendorLoadStatementUseCase(vendorStatementGateway).load(vendorId)) } catch (exception: Exception) { errorMessage = exception.message ?: "Vendor statement is temporarily unavailable." }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Vendor Statement", fontWeight = FontWeight.Bold) }
        when {
            errorMessage != null -> item { Text(errorMessage ?: "Vendor statement is temporarily unavailable.") }
            statement == null -> item { Text("Loading vendor statement...") }
            else -> item { val value = statement ?: return@item; Text("Vendor ID: ${value.vendorId}\nStatus: ${value.statementStatus ?: "—"}\nCurrency: ${value.currency ?: "—"}\nGross: ${amountLabel(value.grossAmount, value.currency)}\nNet: ${amountLabel(value.netAmount, value.currency)}") }
        }
    }
}

private fun amountLabel(amount: Double, currency: String?): String = listOfNotNull(currency?.takeIf { it.isNotBlank() }, String.format("%.2f", amount)).joinToString(" ")

