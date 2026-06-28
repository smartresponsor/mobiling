package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.CartCheckoutHandoffPayload
import app.mobiling.client.data.cart.CartCheckoutGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartPrepareCheckoutHandoffUseCase(private val gateway: CartCheckoutGateway) {
    suspend operator fun invoke(): CartCheckoutHandoffPayload = gateway.prepareCheckoutHandoff()
}
