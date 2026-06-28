package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessResetRecoveryUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessResetRecoveryRequest): AccessAuthSessionPayload =
        gateway.resetRecovery(request)
}
