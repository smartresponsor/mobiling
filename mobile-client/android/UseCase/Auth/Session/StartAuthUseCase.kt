package app.mobiling.client.client.usecase.auth.session

import app.mobiling.client.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.client.contract.auth.session.StartAuthRequest
import app.mobiling.client.client.data.auth.session.AuthSessionGateway

class StartAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: StartAuthRequest): AuthSessionPayload = gateway.startAuth(request)
}
