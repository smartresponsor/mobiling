package app.mobiling.client.contract.auth.session

data class VerifySecondFactorRequest(
    val code: String,
)
