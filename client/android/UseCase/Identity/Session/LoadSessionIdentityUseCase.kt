package app.mobiling.client.usecase.identity.session

import app.mobiling.client.contract.identity.session.SessionIdentityPayload
import app.mobiling.client.data.identity.session.SessionIdentityGateway

class LoadSessionIdentityUseCase(
    private val gateway: SessionIdentityGateway,
) {
    suspend operator fun invoke(): SessionIdentityPayload = gateway.loadSessionIdentity()
}
