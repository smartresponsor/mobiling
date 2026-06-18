package com.smartresponsor.mobile.client.usecase.auth.session

import com.smartresponsor.mobile.client.contract.auth.session.authsessionpayload
import com.smartresponsor.mobile.client.contract.auth.session.startauthrequest
import com.smartresponsor.mobile.client.data.auth.session.authsessiongateway

class StartAuthUseCase(
    private val gateway: AuthSessionGateway,
) {
    suspend operator fun invoke(request: StartAuthRequest): AuthSessionPayload = gateway.startAuth(request)
}
