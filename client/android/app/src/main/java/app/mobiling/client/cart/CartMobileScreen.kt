package app.mobiling.client.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.cart.CartMobilePayload
import kotlinx.coroutines.launch

@Composable
fun CartMobileScreen(cartFeatureBridge: CartFeatureBridge? = null) {
    var cart by remember { mutableStateOf<CartMobilePayload?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            try {
                cart = cartFeatureBridge?.current()
                error = null
            } catch (exception: Exception) {
                error = exception.message ?: "Cart is unavailable."
            }
        }
    }

    LaunchedEffect(cartFeatureBridge) {
        refresh()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Cart", fontWeight = FontWeight.Bold)
        Text(error ?: "Current cart status: ${cart?.status ?: "not loaded"}")
        cart?.items.orEmpty().forEach { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text("${item.quantity} × ${money(item.unitPriceMinor, item.currencyCode)}") },
                trailingContent = { Text(money(item.lineTotalMinor, item.currencyCode)) },
            )
        }
        Text("Total: ${money(cart?.totalMinor ?: 0L, cart?.currencyCode ?: "USD")}")
        Button(onClick = { refresh() }) {
            Text("Refresh")
        }
    }
}

private fun money(amountMinor: Long, currencyCode: String): String =
    "$currencyCode ${amountMinor / 100}.${(amountMinor % 100).toString().padStart(2, '0')}"
