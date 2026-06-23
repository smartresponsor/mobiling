package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.StartAuthRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class StartAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: StartAuthRequest): AuthSessionPayload = gateway.startAuth(request)
}
