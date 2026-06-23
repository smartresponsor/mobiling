package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.ConfirmVerificationRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class ConfirmVerificationUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: ConfirmVerificationRequest): AuthSessionPayload =
        gateway.confirmVerification(request)
}