package app.mobiling.client.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
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
import app.mobiling.client.contract.order.OrderMobileItemPayload
import app.mobiling.client.contract.product.ProductMobileItemPayload
import app.mobiling.client.contract.project.ProjectMobileItemPayload
import app.mobiling.client.data.order.OrderGateway
import app.mobiling.client.data.product.ProductGateway
import app.mobiling.client.data.project.ProjectGateway
import app.mobiling.client.usecase.order.OrderLoadUseCase
import app.mobiling.client.usecase.product.ProductLoadUseCase
import app.mobiling.client.usecase.project.ProjectLoadUseCase
import kotlinx.coroutines.launch

@Composable
fun ProductMobileScreen(vendorId: String?, productId: String?, gateway: ProductGateway?, onRouteSelected: (String) -> Unit) {
    var rows by remember { mutableStateOf<List<ProductMobileItemPayload>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(vendorId, gateway, refresh) { when { vendorId.isNullOrBlank() -> error = "Products require an active vendor session."; gateway == null -> error = "Product gateway is not available."; else -> try { rows = ProductLoadUseCase(gateway).load(vendorId); error = null } catch (exception: Exception) { error = exception.message } } }
    ProductList(
        "Products", rows, error, productId, { it.productId }, { it.title }, { it.status ?: it.priceLabel }, onRouteSelected,
        onUpdate = { id, value -> gateway?.updateProduct(id, mapOf("title" to value, "name" to value)); refresh++ },
        onDelete = { id -> gateway?.deleteProduct(id); refresh++; onRouteSelected("vendor/product") },
    )
}

@Composable
fun OrderMobileScreen(vendorId: String?, orderId: String?, gateway: OrderGateway?, onRouteSelected: (String) -> Unit) {
    var rows by remember { mutableStateOf<List<OrderMobileItemPayload>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(vendorId, gateway, refresh) { when { vendorId.isNullOrBlank() -> error = "Orders require an active vendor session."; gateway == null -> error = "Order gateway is not available."; else -> try { rows = OrderLoadUseCase(gateway).load(vendorId); error = null } catch (exception: Exception) { error = exception.message } } }
    ProductList(
        "Orders", rows, error, orderId, { it.orderId }, { it.reference }, { it.status ?: it.totalLabel }, onRouteSelected,
        onUpdate = { id, value -> gateway?.updateOrder(id, mapOf("reference" to value, "number" to value)); refresh++ },
        onDelete = { id -> gateway?.deleteOrder(id); refresh++; onRouteSelected("vendor/order") },
    )
}

@Composable
fun ProjectMobileScreen(vendorId: String?, projectId: String?, gateway: ProjectGateway?, onRouteSelected: (String) -> Unit) {
    var rows by remember { mutableStateOf<List<ProjectMobileItemPayload>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(vendorId, gateway, refresh) { when { vendorId.isNullOrBlank() -> error = "Projects require an active vendor session."; gateway == null -> error = "Project gateway is not available."; else -> try { rows = ProjectLoadUseCase(gateway).load(vendorId); error = null } catch (exception: Exception) { error = exception.message } } }
    ProductList(
        "Projects", rows, error, projectId, { it.projectId }, { it.title }, { it.status ?: it.location }, onRouteSelected,
        onUpdate = { id, value -> gateway?.updateProject(id, mapOf("title" to value, "name" to value)); refresh++ },
        onDelete = { id -> gateway?.deleteProject(id); refresh++; onRouteSelected("vendor/project") },
    )
}

@Composable
private fun <T> ProductList(
    title: String,
    rows: List<T>?,
    error: String?,
    selectedId: String?,
    id: (T) -> String,
    label: (T) -> String,
    detail: (T) -> String?,
    onRouteSelected: (String) -> Unit,
    onUpdate: suspend (String, String) -> Unit,
    onDelete: suspend (String) -> Unit,
) {
    val selected = rows?.firstOrNull { id(it) == selectedId }
    val scope = rememberCoroutineScope()
    var value by remember(selectedId, selected) { mutableStateOf(selected?.let(label).orEmpty()) }
    var mutationError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun mutate(block: suspend () -> Unit) {
        if (value.isBlank() && selectedId == null) {
            mutationError = "Name is required."
            return
        }
        scope.launch {
            saving = true
            mutationError = null
            try { block() } catch (exception: Exception) { mutationError = exception.message ?: "CRUD operation failed." }
            saving = false
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                mutationError?.let { Text(it) }
                if (selectedId == null) {
                    Button(onClick = { onRouteSelected(routeFor(title, "new")) }) { Text("New ${title.dropLast(1)}") }
                } else {
                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(if (title == "Orders") "Reference" else "Name") }, modifier = Modifier.fillMaxWidth())
                    Button(enabled = !saving, onClick = { mutate { onUpdate(selectedId, value.trim()) } }) { Text("Save changes") }
                    TextButton(enabled = !saving, onClick = { mutate { onDelete(selectedId) } }) { Text("Delete") }
                    TextButton(onClick = { onRouteSelected(routeFor(title, "").removeSuffix("/")) }) { Text("Back to $title") }
                }
            }
        }
        when {
            error != null -> item { Text(error) }
            rows == null -> item { Text("Loading ${title.lowercase()}...") }
            selectedId != null && selected == null -> item { Text("${title.dropLast(1)} was not found.") }
            selected != null -> item { ProductRow(label(selected), detail(selected)) }
            rows.isEmpty() -> item { Text("No ${title.lowercase()} yet.") }
            else -> items(rows, key = id) { row -> ElevatedCard(Modifier.fillMaxWidth().clickable { onRouteSelected(routeFor(title, id(row))) }) { ProductRow(label(row), detail(row)) } }
        }
    }
}

@Composable
private fun ProductRow(label: String, detail: String?) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        detail?.let { Text(it) }
    }
}

private fun routeFor(title: String, id: String): String = when (title) {
    "Products" -> "vendor/product/$id"
    "Orders" -> "vendor/order/$id"
    else -> "vendor/project/$id"
}
