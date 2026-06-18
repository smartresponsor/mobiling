package com.smartresponsor.mobile.client.usecase.auth.session

import com.smartresponsor.mobile.client.contract.auth.session.AuthSessionPayload
import com.smartresponsor.mobile.client.contract.auth.session.StartAuthRequest
import com.smartresponsor.mobile.client.data.auth.session.AuthSessionGateway

class StartAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: StartAuthRequest): AuthSessionPayload = gateway.startAuth(request)
}
