package app.mobiling.client.usecase.auth.session

import app.mobiling.client.data.auth.session.AuthSessionGateway

class LogoutAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke() = gateway.logoutAuth()
}