package app.mobiling.client.client.usecase.identity.session

import app.mobiling.client.client.contract.identity.session.SessionIdentityPayload
import app.mobiling.client.client.data.identity.session.SessionIdentityGateway

class LoadSessionIdentityUseCase(
    private val gateway: SessionIdentityGateway,
) {
    suspend operator fun invoke(): SessionIdentityPayload = gateway.loadSessionIdentity()
}
