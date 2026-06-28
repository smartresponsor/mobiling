package app.mobiling.client.cart

import app.mobiling.client.contract.cart.CartAddItemRequest
import app.mobiling.client.contract.cart.CartCheckoutHandoffPayload
import app.mobiling.client.contract.cart.CartMobilePayload
import app.mobiling.client.data.cart.CartCheckoutGateway
import app.mobiling.client.data.cart.CartReader
import app.mobiling.client.data.cart.CartWriter
import app.mobiling.client.usecase.cart.CartAddItemUseCase
import app.mobiling.client.usecase.cart.CartLoadCurrentUseCase
import app.mobiling.client.usecase.cart.CartPrepareCheckoutHandoffUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartFeatureBridge(
    private val reader: CartReader,
    private val writer: CartWriter,
    private val checkoutGateway: CartCheckoutGateway,
) {
    suspend fun current(): CartMobilePayload =
        CartLoadCurrentUseCase(reader).invoke()

    suspend fun add(request: CartAddItemRequest): CartMobilePayload =
        CartAddItemUseCase(writer).invoke(request)

    suspend fun checkoutHandoff(): CartCheckoutHandoffPayload =
        CartPrepareCheckoutHandoffUseCase(checkoutGateway).invoke()

}
