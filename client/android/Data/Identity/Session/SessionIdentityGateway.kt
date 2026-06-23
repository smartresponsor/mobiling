package app.mobiling.client.data.identity.session

import app.mobiling.client.contract.identity.session.SessionIdentityPayload

interface SessionIdentityGateway {
    suspend fun loadSessionIdentity(): SessionIdentityPayload
}
