package app.mobiling.client.usecase.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessRestoreUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke(): AccessAuthSessionPayload = gateway.restoreAuth()
}
