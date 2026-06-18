package com.smartresponsor.mobile.client.data.system.security

import com.smartresponsor.mobile.client.contract.system.security.signatureheaders
import okhttp3.Request
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class RequestSignatureApplier(private val secret: String) {
    fun sign(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun headersFor(path: String): SignatureHeaders = SignatureHeaders(signatureValue = sign(path))

    fun apply(request: Request): Request {
        val headers = headersFor(request.url.encodedPath)
        return request.newBuilder().header(headers.signatureHeaderName, headers.signatureValue).build()
    }
}
