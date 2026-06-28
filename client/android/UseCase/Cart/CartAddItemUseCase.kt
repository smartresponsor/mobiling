package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.CartAddItemRequest
import app.mobiling.client.contract.cart.CartMobilePayload
import app.mobiling.client.data.cart.CartWriter

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartAddItemUseCase(private val writer: CartWriter) {
    suspend operator fun invoke(request: CartAddItemRequest): CartMobilePayload = writer.addItem(request)
}
