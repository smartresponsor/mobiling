import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// App-level bridge for order-domain controlled rewire.
@available(*, deprecated, message: "Prefer VendorBusinessBridge.ownedOrder(shipment:taxation:) or VendorNavigationBridge.order for vendor-owned navigation; shipment and taxation should be accessed through OrderBusinessBridge.shipment / taxation.")
struct OrderFeatureBridge {
    let listingGateway: OrderListingGateway
    let detailGateway: OrderDetailGateway

    func list(query: ListOrdersQuery) async -> [OrderSummary] {
        await ListOrdersUseCase(listingGateway: listingGateway).invoke(query: query)
    }

    func detail(orderId: String) async -> OrderDetailPayload {
        await LoadOrderDetailUseCase(detailGateway: detailGateway).invoke(orderId: orderId)
    }
}
