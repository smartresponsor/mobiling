package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.MobileCartCheckoutHandoffPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CartCheckoutGateway {
    suspend fun prepareCheckoutHandoff(): MobileCartCheckoutHandoffPayload
}
