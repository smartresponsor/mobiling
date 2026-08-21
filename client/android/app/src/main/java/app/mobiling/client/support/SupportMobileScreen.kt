package app.mobiling.client.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.contract.support.SupportFieldPayload
import app.mobiling.client.contract.support.SupportPagePayload
import app.mobiling.client.design.MobileDesignSystem
import kotlinx.coroutines.launch

@Composable
fun SupportMobileScreen(
    route: String,
    supportFeatureBridge: SupportFeatureBridge?,
    onRouteSelected: (String) -> Unit,
) {
    var page by remember(route) { mutableStateOf<SupportPagePayload?>(null) }
    var loading by remember(route) { mutableStateOf(true) }
    var errorMessage by remember(route) { mutableStateOf<String?>(null) }
    val values = remember(route) { mutableStateMapOf<String, String>() }
    val scope = rememberCoroutineScope()
    val path = "/" + route.trim('/')

    fun applyPage(next: SupportPagePayload) {
        page = next
        values.clear()
        next.fields.forEach { field -> field.value?.let { values[field.name] = it } }
        errorMessage = null
        loading = false
    }

    fun execute(target: String, method: String, fields: Map<String, String> = emptyMap()) {
        val bridge = supportFeatureBridge ?: return
        scope.launch {
            loading = true
            errorMessage = null
            try {
                if (method.equals("POST", ignoreCase = true)) {
                    applyPage(bridge.submit(target, fields))
                } else {
                    onRouteSelected(target.trim('/'))
                }
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "Support is temporarily unavailable."
                loading = false
            }
        }
    }

    LaunchedEffect(route, supportFeatureBridge) {
        loading = true
        errorMessage = null
        if (supportFeatureBridge == null) {
            errorMessage = "Support service is not available."
            loading = false
            return@LaunchedEffect
        }
        try {
            applyPage(supportFeatureBridge.load(path))
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Support is temporarily unavailable."
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(MobileDesignSystem.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs)) {
                Text(page?.title ?: "Support", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                page?.description?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        when {
            loading -> item { Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            errorMessage != null -> item { Text(errorMessage ?: "Support is unavailable.", color = MaterialTheme.colorScheme.error) }
            page != null -> {
                val current = requireNotNull(page)

                items(current.rows, key = { it.id }) { row ->
                    ElevatedCard(
                        onClick = { if (row.href.isNotBlank()) onRouteSelected(row.href.trim('/')) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs),
                        ) {
                            Text("${row.context} · ${row.request}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (row.description.isNotBlank()) Text(row.description, style = MaterialTheme.typography.bodyMedium)
                            Text("${row.availableItems} available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(current.cases, key = { it.reference }) { case ->
                    ElevatedCard(
                        onClick = { if (case.href.isNotBlank()) onRouteSelected(case.href.trim('/')) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.xs),
                        ) {
                            Text(case.reference, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(case.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("${case.context} · ${case.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                val reference = current.reference
                if (reference != null) item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
                        ) {
                            Text(reference, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            current.status?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                            current.businessContext?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            current.category?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            current.descriptionText?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }

                items(current.fields, key = { it.name }) { field ->
                    SupportField(
                        field = field,
                        value = values[field.name].orEmpty(),
                        onValueChange = { values[field.name] = it },
                    )
                }

                current.action?.let { action ->
                    item {
                        Button(
                            onClick = { execute(action, current.method, values.toMap()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Continue")
                        }
                    }
                }

                if (current.actions.isNotEmpty()) item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.sm),
                    ) {
                        current.actions.filter { it.enabled }.forEach { action ->
                            Button(
                                onClick = { execute(action.href, action.method) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(action.label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportField(
    field: SupportFieldPayload,
    value: String,
    onValueChange: (String) -> Unit,
) {
    if (field.options.isEmpty()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(field.label) },
            minLines = if (field.type == "textarea") 4 else 1,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }

    var expanded by remember(field.name) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            val selected = field.options.firstOrNull { it.value == value }?.label
            Text(selected ?: field.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            field.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onValueChange(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}
