package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.CartAddItemRequest
import app.mobiling.client.contract.cart.CartMobilePayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CartWriter {
    suspend fun addItem(request: CartAddItemRequest): CartMobilePayload
}
