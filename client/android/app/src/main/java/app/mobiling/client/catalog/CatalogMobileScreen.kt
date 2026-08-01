package app.mobiling.client.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
fun CatalogMobileScreen(
    catalogFeatureBridge: CatalogFeatureBridge?,
    rootRequest: Int = 0,
) {
    var nodes by remember { mutableStateOf<List<CatalogNodeSummary>>(emptyList()) }
    var path by remember { mutableStateOf<List<CatalogNodeSummary>>(emptyList()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val currentParent = path.lastOrNull()

    LaunchedEffect(rootRequest) {
        if (path.isNotEmpty()) {
            path = emptyList()
        }
    }

    LaunchedEffect(catalogFeatureBridge, currentParent?.nodeId) {
        if (catalogFeatureBridge == null) {
            errorText = "Catalog bridge is not configured."
            return@LaunchedEffect
        }

        try {
            nodes = catalogFeatureBridge.list(
                CatalogListNodeQuery(
                    parentNodeId = currentParent?.nodeId,
                    searchText = null,
                    includeEmptyNodes = true,
                ),
            )
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
        if (path.isNotEmpty()) {
            Text(
                text = "‹ " + if (path.size == 1) "Services" else path[path.lastIndex - 1].title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { path = path.dropLast(1) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            Text(
                text = currentParent?.title.orEmpty(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            HorizontalDivider()
        }

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
                supportingContent = {
                    Text(
                        when {
                            node.childCount > 0 -> "${node.childCount} service types"
                            (node.productCount ?: 0) > 0 -> "${node.productCount ?: 0} services"
                            else -> node.slug ?: "Service type"
                        },
                    )
                },
                trailingContent = {
                    if (node.childCount > 0) Text("›", fontWeight = FontWeight.Bold)
                },
                modifier = if (node.childCount > 0) {
                    Modifier.clickable { path = path + node }
                } else {
                    Modifier
                },
            )
        }
    }
}
