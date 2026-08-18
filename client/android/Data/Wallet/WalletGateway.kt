package app.mobiling.client.data.wallet

data class WalletCurrencyBalance(
    val code: String,
    val availableMinor: Long,
    val reservedMinor: Long,
    val totalMinor: Long,
)

data class WalletBalancePayload(val walletId: String?, val currency: List<WalletCurrencyBalance>)

data class WalletTransactionItem(
    val transactionId: String,
    val type: String,
    val amountMinor: Long,
    val currency: String,
    val postedAt: String,
)

data class WalletTransactionPayload(val item: List<WalletTransactionItem>, val nextCursor: String?)

data class WalletOperationItem(
    val id: String,
    val type: String,
    val status: String,
    val amountMinor: Long,
    val currency: String,
    val transactionId: String?,
    val reversalTransactionId: String?,
    val sourceType: String? = null,
    val sourceId: String? = null,
    val sourceReference: String? = null,
    val destinationReference: String? = null,
    val railReference: String? = null,
)

data class WalletOperationPayload(val item: List<WalletOperationItem>)

data class WalletWithdrawalDestination(
    val id: String,
    val type: String,
    val label: String,
)

data class WalletWithdrawalDestinationPayload(val item: List<WalletWithdrawalDestination>)

interface WalletGateway {
    suspend fun loadBalance(): WalletBalancePayload
    suspend fun loadTransaction(): WalletTransactionPayload
    suspend fun loadFunding(): WalletOperationPayload
    suspend fun loadWithdrawal(): WalletOperationPayload
    suspend fun loadWithdrawal(id: String): WalletOperationItem
    suspend fun loadWithdrawalDestination(): WalletWithdrawalDestinationPayload
    suspend fun requestWithdrawal(amountMinor: Long, currency: String, paymentInstrumentId: String, idempotencyKey: String): WalletOperationItem
    suspend fun cancelWithdrawal(id: String): WalletOperationItem
}
