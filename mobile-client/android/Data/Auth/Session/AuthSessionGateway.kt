package com.smartresponsor.mobile.client.data.auth.session

import com.smartresponsor.mobile.client.contract.auth.session.AuthSessionPayload
import com.smartresponsor.mobile.client.contract.auth.session.StartAuthRequest

interface AuthSessionGateway {
    suspend fun startAuth(request: StartAuthRequest): AuthSessionPayload
}
