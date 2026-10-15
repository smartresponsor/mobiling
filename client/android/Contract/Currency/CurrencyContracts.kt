package app.mobiling.client.contract.currency

data class CurrencyMetadataPayload(
    val code: String,
    val numericCode: String?,
    val minorUnit: Int,
    val symbol: String?,
    val displayName: String?,
)

data class CurrencyCatalogPayload(
    val items: List<CurrencyMetadataPayload>,
)

data class CurrencyChoicePayload(
    val code: String,
    val label: String,
    val symbol: String?,
    val minorUnit: Int,
)

data class CurrencySelectorPayload(
    val choices: List<CurrencyChoicePayload>,
    val selectedCode: String?,
    val placeholder: String,
)

