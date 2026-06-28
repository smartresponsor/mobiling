package app.mobiling.client.access

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload

fun AccessAuthSessionPayload.toAccessScreen(): AccessScreen =
    when {
        requiresVerification -> AccessScreen.VerificationRequired
        requiresSecondFactor -> AccessScreen.SecondFactorRequired
        authenticated -> AccessScreen.Dashboard
        else -> AccessScreen.SignIn
    }
