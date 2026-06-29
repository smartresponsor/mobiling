package app.mobiling.client.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import app.mobiling.client.ui.vendor.summary.VendorMobileSummaryScreenContract
import app.mobiling.client.usecase.vendor.summary.VendorLoadSummaryUseCase

@Composable
fun VendorMobileSummaryScreen(
    vendorId: String?,
    vendorSummaryGateway: VendorSummaryGateway?,
) {
    var summary by remember { mutableStateOf<VendorMobileSummaryScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vendorId, vendorSummaryGateway) {
        summary = null
        errorMessage = null

        if (vendorId.isNullOrBlank()) {
            errorMessage = "Summary requires an active vendor session."
            return@LaunchedEffect
        }

        if (vendorSummaryGateway == null) {
            errorMessage = "Vendor summary gateway is not available."
            return@LaunchedEffect
        }

        try {
            summary = VendorMobileSummaryScreenContract.from(
                VendorLoadSummaryUseCase(vendorSummaryGateway).load(vendorId),
            )
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Vendor summary is temporarily unavailable."
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Vendor Summary", fontWeight = FontWeight.Bold) }

        when {
            errorMessage != null -> item { Text(errorMessage ?: "Vendor summary is temporarily unavailable.") }
            summary == null -> item { Text("Loading vendor summary...") }
            else -> {
                val currentSummary = summary ?: return@LazyColumn

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryField("Vendor ID", currentSummary.vendorId)
                        SummaryField("Brand", currentSummary.brandName)
                        SummaryField("Status", currentSummary.status)
                        SummaryField("Next action", currentSummary.nextAction)
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Profile completion: ${currentSummary.profileCompletionPercent}%")
                        LinearProgressIndicator(
                            progress = currentSummary.profileCompletionPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryField(label: String, value: String?) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value?.takeIf { it.isNotBlank() } ?: "—")
    }
}
