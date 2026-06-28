package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.AddCartItemRequest
import app.mobiling.client.contract.cart.MobileCartPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CartWriter {
    suspend fun addItem(request: AddCartItemRequest): MobileCartPayload
}
