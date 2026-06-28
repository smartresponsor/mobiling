package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.CartMobilePayload
import app.mobiling.client.data.cart.CartReader

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartLoadCurrentUseCase(private val reader: CartReader) {
    suspend operator fun invoke(): CartMobilePayload = reader.currentCart()
}
