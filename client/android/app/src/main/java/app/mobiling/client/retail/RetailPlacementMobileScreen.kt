package app.mobiling.client.retail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.mobiling.client.data.retail.placement.RetailPlacementGateway
import app.mobiling.client.data.retail.placement.RetailPlacementSnapshot
import kotlinx.coroutines.launch

@Composable
fun RetailPlacementMobileScreen(
    retailId: String,
    gateway: RetailPlacementGateway?,
    onRouteSelected: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var snapshot by remember(retailId) { mutableStateOf<RetailPlacementSnapshot?>(null) }
    var values by remember(retailId) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember(retailId) { mutableStateOf<String?>(null) }
    var saving by remember(retailId) { mutableStateOf(false) }

    LaunchedEffect(retailId, gateway) {
        try {
            snapshot = gateway?.snapshot(retailId) ?: error("Retail placement gateway is not available.")
        } catch (exception: Exception) {
            error = exception.message ?: "Retail placement could not be loaded."
        }
    }

    val current = snapshot
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(current?.title ?: "Listing placement", fontWeight = FontWeight.SemiBold)
            Text("Listing #$retailId")
            current?.status?.let { Text("Status: $it") }
        }

        if (current == null) {
            item { Text(error ?: "Loading placement…") }
            return@LazyColumn
        }

        when (current.nextStep) {
            "fulfillment" -> {
                item {
                    PlacementChoice(
                        label = "Fulfillment",
                        key = "mode",
                        choices = fulfillmentModes(current.kind),
                        values = values,
                    ) { values = values + ("mode" to it) }
                }
                item { PlacementField("Service area / region", "serviceArea", values) { values = values + ("serviceArea" to it) } }
                item { PlacementField("Radius (km)", "radiusKm", values) { values = values + ("radiusKm" to it) } }
                if (current.kind == "goods" && values["mode"] == "shipping") {
                    item { PlacementField("Weight (kg)", "weightKg", values) { values = values + ("weightKg" to it) } }
                    item {
                        PlacementChoice(
                            label = "Priority",
                            key = "priority",
                            choices = listOf("STANDARD", "EXPRESS", "OVERNIGHT"),
                            values = values,
                        ) { values = values + ("priority" to it) }
                    }
                }
            }

            "location" -> locationFields().forEach { (key, label) ->
                item(key) { PlacementField(label, key, values) { values = values + (key to it) } }
            }

            "pricing" -> {
                item {
                    PlacementChoice(
                        label = "Pricing",
                        key = "model",
                        choices = pricingModels(current.kind),
                        values = values,
                    ) { values = values + ("model" to it) }
                }
                if (!values["model"].isNullOrBlank() && values["model"] != "quote") {
                    item { PlacementField("Amount (minor units)", "amountMinor", values) { values = values + ("amountMinor" to it) } }
                }
                if (values["model"] == "range") {
                    item { PlacementField("Maximum amount", "maximumAmountMinor", values) { values = values + ("maximumAmountMinor" to it) } }
                }
                item { PlacementField("Currency", "currency", values, "USD") { values = values + ("currency" to it) } }
            }

            "review" -> {
                item { Text("Fulfillment: ${current.fulfillmentProfile ?: "Configured"}") }
                item { Text("Location: ${current.locationProfile ?: "Not required"}") }
                item { Text("Pricing: ${current.pricingProfile ?: "Configured"}") }
            }

            "complete" -> item {
                Button(onClick = { onRouteSelected("vendor/retail") }) { Text("Back to listings") }
            }

            else -> item { Text("Unknown placement step: ${current.nextStep}") }
        }

        error?.let { message -> item { Text(message) } }

        if (current.nextStep != "complete") {
            item {
                TextButton(enabled = !saving, onClick = { onRouteSelected("vendor/retail") }) { Text("Exit") }
                Button(
                    enabled = !saving,
                    onClick = {
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                val activeGateway = gateway ?: error("Retail placement gateway is not available.")
                                val payload = values.mapValues { it.value.trim() }.filterValues { it.isNotBlank() }
                                snapshot = when (current.nextStep) {
                                    "fulfillment" -> activeGateway.configureFulfillment(retailId, payload)
                                    "location" -> activeGateway.configureLocation(retailId, payload)
                                    "pricing" -> activeGateway.configurePricing(retailId, payload)
                                    "review" -> activeGateway.publish(retailId)
                                    else -> current
                                }
                                values = emptyMap()
                            } catch (exception: Exception) {
                                error = exception.message ?: "Placement step could not be saved."
                            }
                            saving = false
                        }
                    },
                ) {
                    Text(if (saving) "Saving…" else if (current.nextStep == "review") "Publish" else "Continue")
                }
            }
        }
    }
}

@Composable
private fun PlacementField(
    label: String,
    key: String,
    values: Map<String, String>,
    default: String = "",
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = values[key] ?: default,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PlacementChoice(
    label: String,
    key: String,
    choices: List<String>,
    values: Map<String, String>,
    onChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        choices.forEach { choice ->
            Row {
                RadioButton(
                    selected = values[key] == choice,
                    onClick = { onChange(choice) },
                )
                Text(choice.lowercase().replaceFirstChar { character -> character.uppercase() })
            }
        }
    }
}

private fun fulfillmentModes(kind: String): List<String> =
    if (kind == "goods") listOf("shipping", "pickup", "digital") else listOf("onsite", "remote", "hybrid")

private fun pricingModels(kind: String): List<String> = when (kind) {
    "service" -> listOf("fixed", "hourly", "minimum", "quote")
    "goods" -> listOf("fixed", "deposit")
    "task" -> listOf("budget", "range", "fixed")
    "project" -> listOf("budget", "range", "fixed", "quote")
    else -> listOf("fixed")
}

private fun locationFields(): List<Pair<String, String>> = listOf(
    "line1" to "Address",
    "line2" to "Address line 2",
    "city" to "City",
    "region" to "State / region",
    "postalCode" to "Postal code",
    "countryCode" to "Country code",
)
