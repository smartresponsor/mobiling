package app.mobiling.client.access

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.mobiling.client.auth.AuthFeatureBridge
import kotlinx.coroutines.launch

@Composable
fun MobilingAppShell(
    authFeatureBridge: AuthFeatureBridge? = null,
) {
    var currentScreen by rememberSaveable { mutableStateOf(AccessScreen.Welcome) }
    val coroutineScope = rememberCoroutineScope()

    fun clearAccessSession() {
        coroutineScope.launch {
            try {
                authFeatureBridge?.logout()
            } catch (_: Exception) {
            }

            currentScreen = AccessScreen.Welcome
        }
    }

    LaunchedEffect(authFeatureBridge) {
        val payload = try {
            authFeatureBridge?.restore()
        } catch (_: Exception) {
            null
        }

        if (payload != null) {
            currentScreen = payload.toAccessScreen()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        when (currentScreen) {
            AccessScreen.Welcome -> AccessWelcomeScreen(
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onCreateAccess = { currentScreen = AccessScreen.Register },
            )

            AccessScreen.SignIn -> SignInScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onCreateAccess = { currentScreen = AccessScreen.Register },
                onStartAccess = { request -> authFeatureBridge?.start(request) },
                onAccessSession = { payload -> currentScreen = payload.toAccessScreen() },
            )

            AccessScreen.Register -> RegisterAccessScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onRegisterAccess = { request -> authFeatureBridge?.register(request) },
                onAccessSession = { payload -> currentScreen = payload.toAccessScreen() },
            )

            AccessScreen.VerificationRequired -> VerificationRequiredScreen(
                onBack = { currentScreen = AccessScreen.SignIn },
                onUseDifferentAccess = { clearAccessSession() },
            )

            AccessScreen.SecondFactorRequired -> SecondFactorRequiredScreen(
                onBack = { currentScreen = AccessScreen.SignIn },
                onUseDifferentAccess = { clearAccessSession() },
            )
        }
    }
}