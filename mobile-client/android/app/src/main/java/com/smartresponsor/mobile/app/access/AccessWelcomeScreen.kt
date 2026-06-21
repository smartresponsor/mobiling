package com.smartresponsor.mobile.app.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccessWelcomeScreen(
    onSignIn: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    val businessAreas = listOf("Vendor", "Catalog", "Order", "Message")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "SmartResponsor",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Business access for vendor, catalog, order, and message workflows.",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Access is required to open the business workspace.",
            style = MaterialTheme.typography.bodyMedium,
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            businessAreas.forEach { area ->
                ElevatedButton(onClick = {}, enabled = false) {
                    Text(area)
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Guest entry", style = MaterialTheme.typography.titleMedium)
                Text("Sign in to continue or create access for a new workspace account.")
            }
        }

        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in")
        }
        OutlinedButton(onClick = onCreateAccess, modifier = Modifier.fillMaxWidth()) {
            Text("Create access")
        }
    }
}
