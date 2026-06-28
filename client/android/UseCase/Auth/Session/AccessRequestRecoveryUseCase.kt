package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessRequestRecoveryUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessRequestRecoveryRequest): AccessAuthSessionPayload =
        gateway.requestRecovery(request)
}
