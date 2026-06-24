package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.RequestRecoveryRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class RequestRecoveryUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: RequestRecoveryRequest): AuthSessionPayload =
        gateway.requestRecovery(request)
}
