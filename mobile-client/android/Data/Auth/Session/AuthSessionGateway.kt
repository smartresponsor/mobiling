package com.smartresponsor.mobile.client.data.auth.session

import com.smartresponsor.mobile.client.contract.auth.session.authsessionpayload
import com.smartresponsor.mobile.client.contract.auth.session.startauthrequest

interface AuthSessionGateway {
    suspend fun startAuth(request: StartAuthRequest): AuthSessionPayload
}
