package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.MobileCartPayload
import app.mobiling.client.data.cart.CartReader

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadCurrentCartUseCase(private val reader: CartReader) {
    suspend operator fun invoke(): MobileCartPayload = reader.currentCart()
}
