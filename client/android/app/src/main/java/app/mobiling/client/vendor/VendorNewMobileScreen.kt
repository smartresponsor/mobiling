package app.mobiling.client.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import androidx.compose.ui.unit.dp
import app.mobiling.client.RetailKind
import kotlinx.coroutines.launch

data class VendorNewField(
    val key: String,
    val label: String,
    val required: Boolean = false,
    val numeric: Boolean = false,
)

private val ProductKindChoices = mapOf(
    RetailKind.Task to "Task — I need something done",
    RetailKind.Service to "Service — I offer my skills",
    RetailKind.Goods to "Product — I am selling an item",
    RetailKind.Project to "Project — I am publishing a project",
)

@Composable
private fun ProjectStoryRichTextEditor(
    initialDocument: String,
    onDocumentChange: (documentJson: String, plainText: String) -> Unit,
    error: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Project story *", fontWeight = FontWeight.SemiBold)
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(340.dp),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = true
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun postMessage(message: String) {
                            val envelope = JSONObject(message)
                            if (envelope.optString("type") != "change") return
                            val payload = envelope.getJSONObject("payload")
                            val json = payload.getJSONObject("json").toString()
                            val text = payload.optString("text")
                            post { onDocumentChange(json, text) }
                        }
                    }, "AndroidRichText")
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            if (initialDocument.isNotBlank()) {
                                val quoted = JSONObject.quote(initialDocument)
                                view.evaluateJavascript("window.MobilingRichText.setContent(JSON.parse($quoted));", null)
                            }
                        }
                    }
                    loadUrl("file:///android_asset/richtext/index.html")
                }
            },
        )
        error?.let { Text(it) }
    }
}

@Composable
fun VendorNewMobileScreen(
    singular: String,
    listRoute: String,
    fields: List<VendorNewField>,
    onCreate: suspend (Map<String, String>) -> Unit,
    onRouteSelected: (String) -> Unit,
    initialValues: Map<String, String> = emptyMap(),
    availableRetailKinds: List<RetailKind> = RetailKind.entries,
) {
    val scope = rememberCoroutineScope()
    var values by remember(fields, initialValues) {
        mutableStateOf(fields.associate { it.key to "" } + initialValues)
    }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        val errors = buildMap {
            fields.forEach { field ->
                val value = values[field.key].orEmpty().trim()
                if (field.required && value.isBlank()) put(field.key, "${field.label} is required.")
                if (field.numeric && value.isNotBlank() && value.toBigDecimalOrNull() == null) put(field.key, "${field.label} must be a number.")
            }
        }
        fieldErrors = errors
        return errors.isEmpty()
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        fields.forEach { field ->
            item(field.key) {
                if (field.key == "kind") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Listing type *", fontWeight = FontWeight.SemiBold)
                        availableRetailKinds.forEach { kind ->
                            val label = ProductKindChoices.getValue(kind)
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(
                                    selected = values["kind"] == kind.code,
                                    onClick = {
                                        values = values + ("kind" to kind.code)
                                        fieldErrors = fieldErrors - "kind"
                                    },
                                )
                                Text(label)
                            }
                        }
                        fieldErrors["kind"]?.let { Text(it) }
                    }
                } else OutlinedTextField(
                    value = values[field.key].orEmpty(),
                    onValueChange = { value ->
                        values = values + (field.key to value)
                        fieldErrors = fieldErrors - field.key
                    },
                    label = { Text(field.label + if (field.required) " *" else "") },
                    supportingText = { fieldErrors[field.key]?.let { Text(it) } },
                    isError = field.key in fieldErrors,
                    singleLine = field.key !in setOf("description", "notes"),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        submitError?.let { message -> item { Text(message) } }
        item {
            Button(
                enabled = !saving,
                onClick = {
                    if (!validate()) return@Button
                    scope.launch {
                        saving = true
                        submitError = null
                        try {
                            onCreate(values.mapValues { it.value.trim() }.filterValues { it.isNotBlank() })
                            onRouteSelected(listRoute)
                        } catch (exception: Exception) {
                            submitError = exception.message ?: "The $singular could not be created."
                        }
                        saving = false
                    }
                },
            ) {
                Text(if (saving) "Creating…" else "Create $singular")
            }
        }
        item {
            TextButton(onClick = { onRouteSelected(listRoute) }) { Text("Cancel") }
        }
    }
}

data class ProjectWizardStep(
    val key: String,
    val title: String,
    val fields: List<VendorNewField>,
    val kinds: Set<String> = emptySet(),
    val review: Boolean = false,
)

private val ProjectKindChoices = listOf(
    "charity_health_life" to "Charity (health/life)",
    "social_non_profit" to "Social (non-profit)",
    "goods_reputation_exchange" to "Goods reputation exchange",
    "business_for_profit" to "Business (for-profit)",
)

private val ProjectWizardSteps = listOf(
    ProjectWizardStep("base", "Basics", listOf(VendorNewField("title", "Title", required = true))),
    ProjectWizardStep("narrative", "Story", listOf(VendorNewField("rawText", "Project story", required = true))),
    ProjectWizardStep("business", "Business", listOf(
        VendorNewField("offer", "Offer", required = true),
        VendorNewField("revenueModel", "Revenue model"),
        VendorNewField("traction", "Traction"),
    ), setOf("business_for_profit")),
    ProjectWizardStep("exchange", "Exchange", listOf(
        VendorNewField("itemSpec", "Goods offered", required = true),
        VendorNewField("handoffRules", "Handoff rules", required = true),
        VendorNewField("geography", "Area, city, or region"),
        VendorNewField("reputationAsk", "Expected reputation credit", required = true),
        VendorNewField("scoringPolicy", "Scoring policy"),
    ), setOf("goods_reputation_exchange")),
    ProjectWizardStep("governance", "Governance", listOf(
        VendorNewField("participationRule", "Participation rules"),
        VendorNewField("impactMetric", "Impact measurement"),
    ), setOf("social_non_profit")),
    ProjectWizardStep("evidence", "Evidence", listOf(
        VendorNewField("evidenceSummary", "Evidence summary"),
        VendorNewField("publicProofNote", "Public proof note"),
    ), setOf("charity_health_life", "goods_reputation_exchange")),
    ProjectWizardStep("risk", "Risk", listOf(VendorNewField("riskNote", "Risks and safeguards")), setOf("charity_health_life", "business_for_profit")),
    ProjectWizardStep("review", "Review", emptyList(), review = true),
)

@Composable
fun ProjectNewMobileScreen(
    onCreate: suspend (Map<String, String>) -> Unit,
    onRouteSelected: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var values by remember { mutableStateOf(mapOf("kind" to "social_non_profit")) }
    var currentStep by remember { mutableStateOf(0) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val kind = values["kind"].orEmpty()
    val visibleSteps = ProjectWizardSteps.filter { it.kinds.isEmpty() || kind in it.kinds }
    val step = visibleSteps[currentStep.coerceIn(0, visibleSteps.lastIndex)]

    fun validateStep(): Boolean {
        val errors = step.fields.mapNotNull { field ->
            val value = values[field.key].orEmpty().trim()
            when {
                field.required && value.isBlank() -> field.key to "${field.label} is required."
                else -> null
            }
        }.toMap()
        fieldErrors = errors
        return errors.isEmpty()
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Step ${currentStep + 1} of ${visibleSteps.size}: ${step.title}")
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / visibleSteps.size.toFloat(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (step.key == "base") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Project kind *", fontWeight = FontWeight.SemiBold)
                    ProjectKindChoices.forEach { (value, label) ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected = kind == value, onClick = {
                                values = values + ("kind" to value)
                                currentStep = 0
                            })
                            Text(label)
                        }
                    }
                }
            }
        }
        step.fields.forEach { field ->
            item(field.key) {
                if (field.key == "rawText") {
                    ProjectStoryRichTextEditor(
                        initialDocument = values["rawTextDocument"].orEmpty(),
                        onDocumentChange = { documentJson, plainText ->
                            values = values + mapOf(
                                "rawTextDocument" to documentJson,
                                field.key to plainText,
                            )
                            fieldErrors = fieldErrors - field.key
                        },
                        error = fieldErrors[field.key],
                    )
                } else {
                    OutlinedTextField(
                        value = values[field.key].orEmpty(),
                        onValueChange = { value ->
                            values = values + (field.key to value)
                            fieldErrors = fieldErrors - field.key
                        },
                        label = { Text(field.label + if (field.required) " *" else "") },
                        supportingText = { fieldErrors[field.key]?.let { Text(it) } },
                        isError = field.key in fieldErrors,
                        singleLine = field.key == "title" || field.key == "geography",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (step.review) {
            items(values.entries.sortedBy { it.key }, key = { it.key }) { entry ->
                if (entry.value.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(entry.key, fontWeight = FontWeight.SemiBold)
                        Text(entry.value)
                    }
                }
            }
        }
        submitError?.let { message -> item { Text(message) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentStep > 0) {
                    TextButton(enabled = !saving, onClick = { currentStep-- }) { Text("Back") }
                } else {
                    TextButton(enabled = !saving, onClick = { onRouteSelected("vendor/project") }) { Text("Cancel") }
                }
                if (currentStep < visibleSteps.lastIndex) {
                    Button(enabled = !saving, onClick = { if (validateStep()) currentStep++ }) { Text("Next") }
                } else {
                    Button(enabled = !saving, onClick = {
                        scope.launch {
                            saving = true
                            submitError = null
                            try {
                                onCreate(values.mapValues { it.value.trim() }.filterValues { it.isNotBlank() })
                                onRouteSelected("vendor/project")
                            } catch (exception: Exception) {
                                submitError = exception.message ?: "The Project could not be created."
                            }
                            saving = false
                        }
                    }) { Text(if (saving) "Creating…" else "Create Project") }
                }
            }
        }
    }
}

val RetailNewFields = listOf(
    VendorNewField("kind", "Listing type", required = true),
    VendorNewField("categoryId", "Category ID", required = true),
    VendorNewField("title", "Title", required = true),
    VendorNewField("description", "Description"),
    VendorNewField("amountMinor", "Budget / price in cents", numeric = true),
    VendorNewField("currency", "Currency", required = true),
    VendorNewField("location", "Location"),
)

val OrderNewFields = listOf(
    VendorNewField("reference", "Reference", required = true),
    VendorNewField("status", "Status"),
    VendorNewField("total", "Total", numeric = true),
)

val ProjectNewFields = listOf(
    VendorNewField("title", "Title", required = true),
    VendorNewField("description", "Description"),
    VendorNewField("location", "Location"),
    VendorNewField("budget", "Budget", numeric = true),
)
