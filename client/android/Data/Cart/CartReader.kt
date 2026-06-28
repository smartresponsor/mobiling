package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.MobileCartPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CartReader {
    suspend fun currentCart(): MobileCartPayload
}
