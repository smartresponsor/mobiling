package com.smartresponsor.mobile.app.order

import com.smartresponsor.mobile.client.contract.order.detail.OrderDetailPayload
import com.smartresponsor.mobile.client.contract.order.listing.ListOrdersQuery
import com.smartresponsor.mobile.client.contract.order.listing.OrderSummary
import com.smartresponsor.mobile.client.data.order.detail.OrderDetailGateway
import com.smartresponsor.mobile.client.data.order.listing.OrderListingGateway
import com.smartresponsor.mobile.client.usecase.order.detail.LoadOrderDetailUseCase
import com.smartresponsor.mobile.client.usecase.order.listing.ListOrdersUseCase

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
    suspend fun list(query: ListOrdersQuery): List<OrderSummary> =
        ListOrdersUseCase(listingGateway).invoke(query)

    suspend fun detail(orderId: String): OrderDetailPayload =
        LoadOrderDetailUseCase(detailGateway).invoke(orderId)
}
