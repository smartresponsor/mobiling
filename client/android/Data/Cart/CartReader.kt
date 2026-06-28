package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.CartMobilePayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CartReader {
    suspend fun currentCart(): CartMobilePayload
}
