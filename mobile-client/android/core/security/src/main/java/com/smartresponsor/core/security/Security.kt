package com.smartresponsor.core.security

import com.smartresponsor.mobile.client.contract.system.security.signatureheaders
import com.smartresponsor.mobile.client.data.system.security.requestsignatureapplier
import com.smartresponsor.mobile.client.usecase.system.security.applyrequestsignatureusecase
import okhttp3.Request

/**
 * Legacy-compatible Android entry point bridged to canonical system/security slices.
 */
class Security(
    secret: String,
) {
    private val requestSignatureApplier: RequestSignatureApplier = RequestSignatureApplier(secret)
    private val applyRequestSignatureUseCase: ApplyRequestSignatureUseCase =
        ApplyRequestSignatureUseCase(requestSignatureApplier)

    fun sign(data: String): String = requestSignatureApplier.sign(data)

    fun headersFor(path: String): SignatureHeaders = requestSignatureApplier.headersFor(path)

    fun apply(request: Request): Request = applyRequestSignatureUseCase(request)
}
