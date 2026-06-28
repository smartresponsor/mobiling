package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessConfirmVerificationUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessConfirmVerificationRequest): AccessAuthSessionPayload =
        gateway.confirmVerification(request)
}