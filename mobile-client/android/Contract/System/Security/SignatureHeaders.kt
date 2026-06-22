package app.mobiling.client.client.contract.system.security

data class SignatureHeaders(
    val signatureHeaderName: String = "X-SR-Signature",
    val signatureValue: String,
)
