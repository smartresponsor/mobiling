package app.mobiling.client.usecase.auth.session

import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

class AccessLogoutUseCase(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend operator fun invoke() = gateway.logoutAuth()
}