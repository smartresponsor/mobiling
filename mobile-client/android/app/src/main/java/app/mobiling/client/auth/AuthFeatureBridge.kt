package app.mobiling.client.auth

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import app.mobiling.client.contract.auth.session.StartAuthRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway
import app.mobiling.client.usecase.auth.session.RegisterAuthUseCase
import app.mobiling.client.usecase.auth.session.StartAuthUseCase

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

    suspend fun register(request: RegisterAuthRequest): AuthSessionPayload =
        RegisterAuthUseCase(gateway).invoke(request)
}
