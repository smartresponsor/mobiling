package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessVerifySecondFactorUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(request: AccessVerifySecondFactorRequest): AccessAuthSessionPayload =
        gateway.verifySecondFactor(request)
}
