package app.mobiling.client.usecase.cart

import app.mobiling.client.contract.cart.AddCartItemRequest
import app.mobiling.client.contract.cart.MobileCartPayload
import app.mobiling.client.data.cart.CartWriter

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AddCartItemUseCase(private val writer: CartWriter) {
    suspend operator fun invoke(request: AddCartItemRequest): MobileCartPayload = writer.addItem(request)
}
