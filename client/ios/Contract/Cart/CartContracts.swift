import Foundation

public struct CartAddItemRequest: Sendable, Codable {
    public let offerReference: String
    public let quantity: Int
    public let title: String?
    public let unitPriceMinor: Int64?
    public let currencyCode: String?

    public init(offerReference: String, quantity: Int, title: String? = nil, unitPriceMinor: Int64? = nil, currencyCode: String? = nil) {
        self.offerReference = offerReference
        self.quantity = quantity
        self.title = title
        self.unitPriceMinor = unitPriceMinor
        self.currencyCode = currencyCode
    }
}

public struct CartItemPayload: Sendable, Codable, Identifiable {
    public let itemId: String
    public let offerReference: String
    public let title: String
    public let unitPriceMinor: Int64
    public let currencyCode: String
    public let quantity: Int
    public let lineTotalMinor: Int64

    public var id: String { itemId }
}

public struct CartMobilePayload: Sendable, Codable {
    public let cartId: String?
    public let cartToken: String
    public let ownerReference: String?
    public let status: String
    public let currencyCode: String
    public let itemCount: Int
    public let subtotalMinor: Int64
    public let totalMinor: Int64
    public let items: [CartItemPayload]
    public let expiresAt: String?
    public let updatedAt: String?
}

public struct CartCheckoutHandoffPayload: Sendable, Codable {
    public let cartId: String?
    public let cartToken: String
    public let handoffId: String
    public let checkoutUrl: String?
    public let status: String
    public let expiresAt: String?
}
