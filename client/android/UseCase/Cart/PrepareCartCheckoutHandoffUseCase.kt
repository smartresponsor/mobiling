package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.MobileCartCheckoutHandoffPayload
import app.mobiling.client.data.cart.CartCheckoutGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class PrepareCartCheckoutHandoffUseCase(private val gateway: CartCheckoutGateway) {
    suspend operator fun invoke(): MobileCartCheckoutHandoffPayload = gateway.prepareCheckoutHandoff()
}
