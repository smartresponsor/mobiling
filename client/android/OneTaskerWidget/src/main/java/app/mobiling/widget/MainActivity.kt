package app.mobiling.widget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.notification.NotificationInboxItem
import app.mobiling.client.contract.notification.RecognizedNeedRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OneTaskerWidgetRoot() }
    }
}

private enum class WidgetSection {
    LISTEN,
    NEEDS,
    PROFILE,
}

@Composable
private fun OneTaskerWidgetRoot() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val graph = remember(context) { WidgetApplicationGraph.current(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Checking session…") }
    var needs by remember { mutableStateOf<List<NotificationInboxItem>>(emptyList()) }
    var selectedSection by remember { mutableStateOf(WidgetSection.LISTEN) }
    var authenticated by remember { mutableStateOf(false) }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    fun openWeb(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    suspend fun refreshNeeds(): String {
        val inbox = graph.notificationGateway.inbox()
        needs = inbox.items.filter { it.eventName == "need.recognized" && it.topic == "one_tasker.need" }
        return "${needs.size} needs · ${inbox.unreadCount} unread"
    }

    suspend fun listenAndCaptureNeed(): String {
        val localeTag = Locale.getDefault().toLanguageTag()
        val speech = graph.speechRecognition.recognizeOnce(localeTag)
        val transcript = speech.transcript.trim()
        if (transcript.isBlank()) return "No speech recognized"
        val recognized = graph.needClassifier.classify(transcript, localeTag)
        if (!recognized.isNeed) return "No service need recognized"
        val summary = recognized.summary?.trim().takeUnless { it.isNullOrBlank() }
            ?: return "Need classifier returned no summary"
        val category = recognized.category?.trim().takeUnless { it.isNullOrBlank() }
            ?: return "Need classifier returned no category"
        val created = graph.notificationGateway.recognizedNeed(
            RecognizedNeedRequest(
                summary = summary,
                category = category,
                confidence = recognized.confidence,
                locale = localeTag,
                classifierVersion = recognized.classifierVersion,
            ),
        )
        if (!created) return "Unable to save recognized need"
        refreshNeeds()
        selectedSection = WidgetSection.NEEDS
        return summary
    }

    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            status = "Microphone permission is required for Listen"
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            status = runCatching { listenAndCaptureNeed() }
                .getOrElse { "Need classification unavailable" }
        }
    }

    LaunchedEffect(graph) {
        status = runCatching {
            val session = graph.authGateway.restoreAuth()
            authenticated = session.authenticated
            if (!session.authenticated) {
                "Sign in to see your needs"
            } else {
                val inbox = graph.notificationGateway.inbox()
                needs = inbox.items.filter { it.eventName == "need.recognized" && it.topic == "one_tasker.need" }
                "${needs.size} needs · ${inbox.unreadCount} unread"
            }
        }.getOrElse { "Offline · tap Listen when ready" }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("OneTasker")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { selectedSection = WidgetSection.LISTEN }) { Text("Listen") }
                    Button(onClick = { selectedSection = WidgetSection.NEEDS }) { Text("Needs") }
                    Button(onClick = { selectedSection = WidgetSection.PROFILE }) { Text("Profile") }
                }

                when (selectedSection) {
                    WidgetSection.LISTEN -> {
                        Text("Describe what you need")
                        Button(onClick = {
                            if (!authenticated) {
                                selectedSection = WidgetSection.PROFILE
                                status = "Sign in before describing a need"
                            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    status = runCatching { listenAndCaptureNeed() }
                                        .getOrElse { "Need classification unavailable" }
                                }
                            } else {
                                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }) {
                            Text("Push to talk")
                        }
                        Text("Audio is used for speech recognition and is not stored as a OneTasker business record.")
                    }

                    WidgetSection.NEEDS -> {
                        if (needs.isEmpty()) {
                            Text("No recognized needs yet")
                        }
                        needs.forEach { need ->
                            Text(need.title)
                            need.needCategory?.let { Text("Category: $it") }
                            need.needConfidence?.let { confidence ->
                                Text("Confidence: ${(confidence.coerceIn(0.0, 1.0) * 100).toInt()}%")
                            }
                            need.needLocale?.let { Text("Language: $it") }
                            if (need.status == "new") {
                                Text("New")
                            }
                            if (need.status == "acked") {
                                Text("Callback requested")
                            } else {
                                Button(onClick = {
                                    scope.launch {
                                        val requested = runCatching { graph.notificationGateway.requestNeedCallback(need.id) }
                                            .getOrDefault(false)
                                        if (requested) {
                                            needs = needs.map { item ->
                                                if (item.id == need.id) item.copy(status = "acked") else item
                                            }
                                            status = "Callback requested"
                                        } else {
                                            status = "Unable to request callback"
                                        }
                                    }
                                }) {
                                    Text("Call me")
                                }
                            }
                        }
                    }

                    WidgetSection.PROFILE -> {
                        Text(if (authenticated) "Account connected" else "Account sign-in required")
                        if (!authenticated) {
                            OutlinedTextField(
                                value = login,
                                onValueChange = { login = it },
                                label = { Text("Email") },
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                            )
                            Button(onClick = {
                                val normalizedLogin = login.trim()
                                if (normalizedLogin.isBlank() || password.isBlank()) {
                                    status = "Email and password are required"
                                } else {
                                    scope.launch {
                                        val result = runCatching {
                                            graph.authGateway.startAuth(
                                                AccessStartAuthRequest(
                                                    login = normalizedLogin,
                                                    password = password,
                                                    deviceLabel = "OneTasker Widget",
                                                ),
                                            )
                                        }
                                        password = ""
                                        val session = result.getOrNull()
                                        when {
                                            session?.authenticated == true -> {
                                                authenticated = true
                                                status = refreshNeeds()
                                            }
                                            session?.requiresVerification == true -> status = "Account verification required"
                                            session?.requiresSecondFactor == true -> status = "Second-factor verification required"
                                            else -> status = "Unable to sign in"
                                        }
                                    }
                                }
                            }) { Text("Sign in") }
                        }
                        if (authenticated) {
                            Button(onClick = {
                                scope.launch {
                                    val signedOut = runCatching {
                                        graph.authGateway.logoutAuth()
                                        true
                                    }.getOrDefault(false)
                                    if (signedOut) {
                                        authenticated = false
                                        needs = emptyList()
                                        selectedSection = WidgetSection.LISTEN
                                        status = "Signed out"
                                    } else {
                                        status = "Unable to sign out"
                                    }
                                }
                            }) { Text("Sign out") }
                        }
                        Text("Speech mode: native/system recognition; platform services may use network processing.")
                        Text("Need classification may send a bounded transcript fragment to the configured product AI service.")
                        Text("OneTasker does not durably store raw audio or unrestricted transcripts in the recognized-need contract.")
                        Text("Language: ${Locale.getDefault().toLanguageTag()}")
                        Button(onClick = {
                            val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:${context.packageName}"))
                            }
                            context.startActivity(settingsIntent)
                        }) { Text("Notification settings") }
                        Button(onClick = { openWeb(BuildConfig.PRIVACY_POLICY_URL) }) { Text("Privacy Policy") }
                        Button(onClick = { openWeb(BuildConfig.TERMS_OF_SERVICE_URL) }) { Text("Terms of Service") }
                        Button(onClick = { openWeb(BuildConfig.ABOUT_URL) }) { Text("About") }
                    }
                }

                Text(status)
            }
        }
    }
}
