package app.mobiling.client.access

import app.mobiling.client.contract.auth.session.AuthSessionPayload

fun AuthSessionPayload.toAccessScreen(): AccessScreen =
    when {
        requiresVerification -> AccessScreen.VerificationRequired
        requiresSecondFactor -> AccessScreen.SecondFactorRequired
        authenticated -> AccessScreen.Welcome
        else -> AccessScreen.SignIn
    }
