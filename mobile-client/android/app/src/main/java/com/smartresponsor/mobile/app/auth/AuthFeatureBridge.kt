package com.smartresponsor.mobile.app.auth

import com.smartresponsor.mobile.client.contract.auth.session.AuthSessionPayload
import com.smartresponsor.mobile.client.contract.auth.session.StartAuthRequest
import com.smartresponsor.mobile.client.data.auth.session.AuthSessionGateway
import com.smartresponsor.mobile.client.usecase.auth.session.StartAuthUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for auth entry flow.
 *
 * It keeps Auth as the dedicated entry/auth surface while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
class AuthFeatureBridge(
    private val gateway: AuthSessionGateway,
) {
    suspend fun start(request: StartAuthRequest): AuthSessionPayload =
        StartAuthUseCase(gateway).invoke(request)
}
