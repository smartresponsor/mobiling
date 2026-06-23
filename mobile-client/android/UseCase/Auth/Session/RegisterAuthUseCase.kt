package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class RegisterAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: RegisterAuthRequest): AuthSessionPayload = gateway.registerAuth(request)
}
