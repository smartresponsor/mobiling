import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct OrderDetailPayload {
    let orderId: String
    let orderNumber: String
    let stateCode: String
    let totalLabel: String
    let subtotalLabel: String
    let taxationLabel: String?
    let shippingLabel: String?
    let placedAtIso: String
    let lineItems: [OrderLineItemPayload]
}

struct OrderLineItemPayload {
    let lineId: String
    let productId: String
    let productTitle: String
    let quantity: Int
    let lineTotalLabel: String
}
