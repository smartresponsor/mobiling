package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessStartUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessStartAuthRequest): AccessAuthSessionPayload = gateway.startAuth(request)
}
