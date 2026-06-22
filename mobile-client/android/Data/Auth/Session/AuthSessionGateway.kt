package app.mobiling.client.client.data.auth.session

import app.mobiling.client.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.client.contract.auth.session.StartAuthRequest

interface AuthSessionGateway {
    suspend fun startAuth(request: StartAuthRequest): AuthSessionPayload
}
