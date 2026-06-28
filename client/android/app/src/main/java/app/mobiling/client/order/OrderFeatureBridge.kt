package app.mobiling.client.order

import app.mobiling.client.contract.order.detail.OrderDetailPayload
import app.mobiling.client.contract.order.listing.OrderListQuery
import app.mobiling.client.contract.order.listing.OrderSummary
import app.mobiling.client.data.order.detail.OrderDetailGateway
import app.mobiling.client.data.order.listing.OrderListingGateway
import app.mobiling.client.usecase.order.detail.OrderLoadDetailUseCase
import app.mobiling.client.usecase.order.listing.OrderListUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for order-domain controlled rewire.
 * It preserves a stable order feature entry point while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
@Deprecated(
    message = "Prefer VendorBusinessBridge.ownedOrder(...) or VendorNavigationBridge.order() for vendor-owned navigation; shipment and taxation should be accessed through OrderBusinessBridge.shipment()/taxation().",
    replaceWith = ReplaceWith("vendorBusinessBridge.ownedOrder(shipment, taxation)")
)
class OrderFeatureBridge(
    private val listingGateway: OrderListingGateway,
    private val detailGateway: OrderDetailGateway,
) {
    suspend fun list(query: OrderListQuery): List<OrderSummary> =
        OrderListUseCase(listingGateway).invoke(query)

    suspend fun detail(orderId: String): OrderDetailPayload =
        OrderLoadDetailUseCase(detailGateway).invoke(orderId)
}
