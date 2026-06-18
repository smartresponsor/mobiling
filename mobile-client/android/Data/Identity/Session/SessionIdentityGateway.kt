package com.smartresponsor.mobile.client.data.identity.session

import com.smartresponsor.mobile.client.contract.identity.session.SessionIdentityPayload

interface SessionIdentityGateway {
    suspend fun loadSessionIdentity(): SessionIdentityPayload
}
