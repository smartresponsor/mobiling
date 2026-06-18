package com.smartresponsor.mobile.client.usecase.system.security

import com.smartresponsor.mobile.client.data.system.security.requestsignatureapplier
import okhttp3.Request

class ApplyRequestSignatureUseCase(private val requestSignatureApplier: RequestSignatureApplier) {
    operator fun invoke(request: Request): Request = requestSignatureApplier.apply(request)
}
