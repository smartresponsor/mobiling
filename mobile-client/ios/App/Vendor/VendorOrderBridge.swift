import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-owned order bridge.
struct VendorOrderBridge {
    let business: OrderBusinessBridge

    func list(query: ListOrdersQuery) async -> [OrderSummary] {
        await business.order().list(query: query)
    }

    func detail(orderId: String) async -> OrderDetailPayload {
        await business.order().detail(orderId: orderId)
    }

    func ownership() -> OrderBusinessBridge {
        business
    }

    func ownedFlows() -> [OrderOwnedFlow] {
        OrderOwnedRouteMap().ownedFlows()
    }
}
