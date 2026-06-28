package app.mobiling.client.cart

import app.mobiling.client.contract.cart.AddCartItemRequest
import app.mobiling.client.contract.cart.MobileCartCheckoutHandoffPayload
import app.mobiling.client.contract.cart.MobileCartPayload
import app.mobiling.client.data.cart.CartCheckoutGateway
import app.mobiling.client.data.cart.CartReader
import app.mobiling.client.data.cart.CartWriter
import app.mobiling.client.usecase.cart.AddCartItemUseCase
import app.mobiling.client.usecase.cart.LoadCurrentCartUseCase
import app.mobiling.client.usecase.cart.PrepareCartCheckoutHandoffUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartFeatureBridge(
    private val reader: CartReader,
    private val writer: CartWriter,
    private val checkoutGateway: CartCheckoutGateway,
) {
    suspend fun current(): MobileCartPayload =
        LoadCurrentCartUseCase(reader).invoke()

    suspend fun add(request: AddCartItemRequest): MobileCartPayload =
        AddCartItemUseCase(writer).invoke(request)

    suspend fun checkoutHandoff(): MobileCartCheckoutHandoffPayload =
        PrepareCartCheckoutHandoffUseCase(checkoutGateway).invoke()

}
