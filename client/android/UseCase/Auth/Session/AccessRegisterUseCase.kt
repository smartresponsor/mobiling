package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessRegisterUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = gateway.registerAuth(request)
}
