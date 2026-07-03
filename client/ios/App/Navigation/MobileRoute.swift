import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public enum MobileRoute: Sendable, Equatable {
    case accessSignIn
    case accessRegister
    case accessRecoveryRequest
    case accessRecoveryReset
    case accessVerification
    case accessPassword
    case accessSignOut
    case dashboard
    case more
    case vendor
    case vendorProfile
    case vendorSummary
    case vendorStatement
    case vendorPayout
    case vendorTransaction
    case vendorAttachment
    case catalog
    case catalogBrowse
    case catalogNode(catalogNodeId: String)
    case catalogSearch(searchText: String?)
    case message
    case messageThread(threadId: String)
    case attachment
    case attachmentDetail(attachmentId: String)
    case cart
    case cartCheckout
    case cartCheckoutResult(checkoutId: String)
    case vendorProduct
    case vendorProductDetail(productId: String)
    case vendorOrder
    case vendorOrderDetail(orderId: String)
    case vendorOrderShipment(orderId: String)
    case vendorOrderShipmentDetail(orderId: String, shipmentId: String)
    case vendorOrderTax(orderId: String)
    case vendorOrderTaxDetail(orderId: String, taxId: String)
}
