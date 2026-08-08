import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public enum MobileRouteResolver {
    public static func resolve(_ rawRoute: String?, query: [String: String] = [:]) -> MobileRoute? {
        let route = normalizeRoute(rawRoute)
        let segments = route.split(separator: "/").map(String.init)

        switch route {
        case "dashboard": return .dashboard
        case "more": return .more
        case "access/sign-in": return .accessSignIn
        case "access/register": return .accessRegister
        case "access/recovery/request": return .accessRecoveryRequest
        case "access/recovery/reset": return .accessRecoveryReset
        case "access/verification": return .accessVerification
        case "access/password": return .accessPassword
        case "access/sign-out": return .accessSignOut
        case "vendor": return .vendor
        case "vendor/page": return .vendorProfile
        case "vendor/summary": return .vendorSummary
        case "vendor/statement": return .vendorStatement
        case "vendor/payout": return .vendorPayout
        case "vendor/transaction": return .vendorTransaction
        case "vendor/attachment": return .vendorAttachment
        case "catalog": return .catalog
        case "catalog/browse": return .catalogBrowse
        case "catalog/search": return .catalogSearch(searchText: query["q"] ?? query["searchText"])
        case "message": return .message
        case "notification": return .notification
        case "attachment": return .attachment
        case "cart": return .cart
        case "cart/checkout": return .cartCheckout
        case "vendor/product": return .vendorProduct
        case "vendor/product/new": return .vendorProductNew
        case "vendor/order": return .vendorOrder
        case "vendor/order/new": return .vendorOrderNew
        case "vendor/project": return .vendorProject
        case "vendor/project/new": return .vendorProjectNew
        default:
            return resolveSegmentedRoute(segments)
        }
    }

    public static func resolve(_ link: MobileLink) -> MobileRoute? {
        resolve(link.route, query: link.query)
    }

    public static func isKnownRoute(_ rawRoute: String?) -> Bool {
        resolve(rawRoute) != nil
    }

    public static func isSignOutAction(action: String?, route rawRoute: String?) -> Bool {
        action == "access.sign_out" || resolve(rawRoute) == .accessSignOut
    }

    public static func isCurrentlyRenderable(_ rawRoute: String?) -> Bool {
        switch resolve(rawRoute) {
        case .dashboard,
             .more,
             .vendor,
             .vendorProfile,
             .vendorSummary,
             .vendorStatement,
             .vendorPayout,
             .vendorTransaction,
             .vendorAttachment,
             .vendorProduct,
             .vendorProductNew,
             .vendorProductDetail,
             .vendorOrder,
             .vendorOrderNew,
             .vendorOrderDetail,
             .vendorOrderShipment,
             .vendorOrderShipmentDetail,
             .vendorOrderTax,
             .vendorOrderTaxDetail,
             .vendorProject,
             .vendorProjectNew,
             .vendorProjectDetail,
             .message,
             .notification,
             .catalog,
             .attachment:
            return true
        default:
            return false
        }
    }

    public static func normalizeRoute(_ rawRoute: String?) -> String {
        let normalized = (rawRoute ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .split(separator: "/", omittingEmptySubsequences: true)
            .joined(separator: "/")

        return normalized == "vendor/profile" ? "vendor/page" : normalized
    }

    private static func resolveSegmentedRoute(_ segments: [String]) -> MobileRoute? {
        if segments.count == 3, segments[0] == "catalog", segments[1] == "node" {
            return .catalogNode(catalogNodeId: segments[2])
        }
        if segments.count == 3, segments[0] == "message", segments[1] == "thread" {
            return .messageThread(threadId: segments[2])
        }
        if segments.count == 2, segments[0] == "attachment" {
            return .attachmentDetail(attachmentId: segments[1])
        }
        if segments.count == 4, segments[0] == "cart", segments[1] == "checkout", segments[2] == "result" {
            return .cartCheckoutResult(checkoutId: segments[3])
        }
        if segments.count == 3, segments[0] == "vendor", segments[1] == "product" {
            return .vendorProductDetail(productId: segments[2])
        }
        if segments.count == 3, segments[0] == "vendor", segments[1] == "order" {
            return .vendorOrderDetail(orderId: segments[2])
        }
        if segments.count == 4, segments[0] == "vendor", segments[1] == "order", segments[3] == "shipment" {
            return .vendorOrderShipment(orderId: segments[2])
        }
        if segments.count == 5, segments[0] == "vendor", segments[1] == "order", segments[3] == "shipment" {
            return .vendorOrderShipmentDetail(orderId: segments[2], shipmentId: segments[4])
        }
        if segments.count == 4, segments[0] == "vendor", segments[1] == "order", segments[3] == "tax" {
            return .vendorOrderTax(orderId: segments[2])
        }
        if segments.count == 5, segments[0] == "vendor", segments[1] == "order", segments[3] == "tax" {
            return .vendorOrderTaxDetail(orderId: segments[2], taxId: segments[4])
        }
        if segments.count == 3, segments[0] == "vendor", segments[1] == "project" {
            return .vendorProjectDetail(projectId: segments[2])
        }
        return nil
    }
}
