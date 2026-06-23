package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.data.auth.session.AuthSessionGateway

class RestoreAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(): AuthSessionPayload = gateway.restoreAuth()
}
