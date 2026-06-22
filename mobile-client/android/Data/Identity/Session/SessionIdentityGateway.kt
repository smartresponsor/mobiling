package app.mobiling.client.client.data.identity.session

import app.mobiling.client.client.contract.identity.session.SessionIdentityPayload

interface SessionIdentityGateway {
    suspend fun loadSessionIdentity(): SessionIdentityPayload
}
