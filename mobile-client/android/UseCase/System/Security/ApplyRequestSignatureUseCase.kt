package app.mobiling.client.usecase.system.security

import app.mobiling.client.data.system.security.RequestSignatureApplier
import okhttp3.Request

class ApplyRequestSignatureUseCase(private val requestSignatureApplier: RequestSignatureApplier) {
    operator fun invoke(request: Request): Request = requestSignatureApplier.apply(request)
}
