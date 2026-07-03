package app.mobiling.client.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
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
import app.mobiling.client.contract.catalog.browse.CatalogListNodeQuery
import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary

@Composable
fun CatalogMobileScreen(catalogFeatureBridge: CatalogFeatureBridge?) {
    var nodes by remember { mutableStateOf<List<CatalogNodeSummary>>(emptyList()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(catalogFeatureBridge) {
        if (catalogFeatureBridge == null) {
            errorText = "Catalog bridge is not configured."
            return@LaunchedEffect
        }

        try {
            nodes = catalogFeatureBridge.list(CatalogListNodeQuery(parentNodeId = null, searchText = null, includeEmptyNodes = true))
            errorText = null
        } catch (exception: Exception) {
            nodes = emptyList()
            errorText = exception.message ?: "Catalog could not be loaded."
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Catalog", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
        val currentError = errorText
        if (currentError != null) {
            Text(currentError, modifier = Modifier.padding(horizontal = 16.dp))
        }
        if (nodes.isEmpty() && currentError == null) {
            Text("No catalog nodes are available.", modifier = Modifier.padding(horizontal = 16.dp))
        }
        nodes.forEach { node ->
            ListItem(
                headlineContent = { Text(node.title) },
                supportingContent = { Text(node.slug ?: node.nodeId) },
            )
        }
    }
}
