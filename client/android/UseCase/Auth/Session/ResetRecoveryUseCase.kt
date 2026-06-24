package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.ResetRecoveryRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class ResetRecoveryUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: ResetRecoveryRequest): AuthSessionPayload =
        gateway.resetRecovery(request)
}
