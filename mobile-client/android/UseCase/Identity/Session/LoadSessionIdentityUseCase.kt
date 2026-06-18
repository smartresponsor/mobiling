package com.smartresponsor.mobile.client.usecase.identity.session

import com.smartresponsor.mobile.client.contract.identity.session.SessionIdentityPayload
import com.smartresponsor.mobile.client.data.identity.session.SessionIdentityGateway

class LoadSessionIdentityUseCase(
    private val gateway: SessionIdentityGateway,
) {
    suspend operator fun invoke(): SessionIdentityPayload = gateway.loadSessionIdentity()
}
