package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.VerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway

class VerifySecondFactorUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: VerifySecondFactorRequest): AuthSessionPayload =
        gateway.verifySecondFactor(request)
}
