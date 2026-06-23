package app.mobiling.client.data.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import app.mobiling.client.contract.auth.session.StartAuthRequest

interface AuthSessionGateway {
    suspend fun startAuth(request: StartAuthRequest): AuthSessionPayload
    suspend fun registerAuth(request: RegisterAuthRequest): AuthSessionPayload
    suspend fun restoreAuth(): AuthSessionPayload
}
