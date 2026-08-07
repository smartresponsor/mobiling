package app.mobiling.client.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.catalog.browse.CatalogListNodeQuery
import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary

@Composable
fun CatalogMobileScreen(catalogFeatureBridge: CatalogFeatureBridge?) {
    var nodes by remember { mutableStateOf<List<CatalogNodeSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    val navigationStack = remember { mutableStateListOf<CatalogNodeSummary>() }
    val parentNodeId = navigationStack.lastOrNull()?.nodeId

    LaunchedEffect(catalogFeatureBridge, parentNodeId, reloadKey) {
        loading = true
        errorText = null

        if (catalogFeatureBridge == null) {
            nodes = emptyList()
            loading = false
            errorText = "Catalog is temporarily unavailable."
            return@LaunchedEffect
        }

        try {
            nodes = catalogFeatureBridge.list(
                CatalogListNodeQuery(
                    parentNodeId = parentNodeId,
                    searchText = null,
                    includeEmptyNodes = true,
                ),
            )
        } catch (exception: Exception) {
            nodes = emptyList()
            errorText = exception.message ?: "Catalog could not be loaded."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CatalogHeader(
            title = navigationStack.lastOrNull()?.title ?: "Explore catalogs",
            subtitle = if (navigationStack.isEmpty()) {
                "Find work, orders, products, and professional services."
            } else {
                "Choose a category to continue."
            },
            canGoBack = navigationStack.isNotEmpty(),
            onBack = { if (navigationStack.isNotEmpty()) navigationStack.removeAt(navigationStack.lastIndex) },
        )

        when {
            loading -> Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }

            errorText != null -> CatalogMessage(
                title = "We could not load this catalog",
                message = errorText.orEmpty(),
                actionLabel = "Try again",
                onAction = { reloadKey += 1 },
            )

            nodes.isEmpty() -> CatalogMessage(
                title = "Nothing here yet",
                message = "New categories and listings will appear here as they become available.",
                actionLabel = if (navigationStack.isEmpty()) null else "Back to catalogs",
                onAction = {
                    if (navigationStack.isNotEmpty()) navigationStack.clear()
                },
            )

            else -> nodes.forEach { node ->
                CatalogNodeCard(
                    node = node,
                    onClick = {
                        if (node.childCount > 0) {
                            navigationStack.add(node)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CatalogHeader(
    title: String,
    subtitle: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (canGoBack) {
            Text(
                text = "‹ Back",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onBack),
            )
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CatalogNodeCard(node: CatalogNodeSummary, onClick: () -> Unit) {
    val interactive = node.childCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = interactive, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(catalogSymbol(node), style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(node.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    catalogDescription(node),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    catalogCountLabel(node),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (interactive) {
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun CatalogMessage(title: String, message: String, actionLabel: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun catalogSymbol(node: CatalogNodeSummary): String {
    val value = "${node.title} ${node.slug.orEmpty()}".lowercase()
    return when {
        "task" in value -> "🧰"
        "order" in value -> "📋"
        "product" in value || "merch" in value -> "📦"
        "service" in value -> "🛠️"
        "appliance" in value -> "🏠"
        "furniture" in value -> "🪑"
        "repair" in value -> "🔧"
        "install" in value -> "⚙️"
        else -> "🗂️"
    }
}

private fun catalogDescription(node: CatalogNodeSummary): String {
    val value = "${node.title} ${node.slug.orEmpty()}".lowercase()
    return when {
        "task" in value -> "Customer requests ready for local professionals."
        "order" in value -> "Active and packaged work requested by customers."
        "product" in value || "merch" in value -> "Tools, parts, fixtures, and marketplace goods."
        "service" in value -> "Professional services offered by verified vendors."
        node.childCount > 0 -> "Browse ${node.childCount} related categories."
        else -> "Open this catalog section."
    }
}

private fun catalogCountLabel(node: CatalogNodeSummary): String = when {
    node.childCount > 0 -> "${node.childCount} categories"
    node.productCount != null -> "${node.productCount} listings"
    else -> "View section"
}
