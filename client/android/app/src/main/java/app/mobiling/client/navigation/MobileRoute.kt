package app.mobiling.client.navigation

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
sealed class MobileRoute(open val routePath: String) {
    sealed class Access(routePath: String) : MobileRoute(routePath) {
        object SignIn : Access("access/sign-in")
        object Register : Access("access/register")
        object RecoveryRequest : Access("access/recovery/request")
        object RecoveryReset : Access("access/recovery/reset")
        object Verification : Access("access/verification")
        object Password : Access("access/password")
        object SignOut : Access("access/sign-out")
    }

    object Dashboard : MobileRoute("dashboard")
    object More : MobileRoute("more")

    sealed class Vendor(routePath: String) : MobileRoute(routePath) {
        object Overview : Vendor("vendor")
        object Profile : Vendor("vendor/profile")
        object Summary : Vendor("vendor/summary")
        object Statement : Vendor("vendor/statement")
        object Payout : Vendor("vendor/payout")
        object Transaction : Vendor("vendor/transaction")
        object Attachment : Vendor("vendor/attachment")
    }

    sealed class Catalog(routePath: String) : MobileRoute(routePath) {
        object Root : Catalog("catalog")
        object Browse : Catalog("catalog/browse")
        data class Node(val catalogNodeId: String) : Catalog("catalog/node/$catalogNodeId")
        data class Search(val searchText: String?) : Catalog("catalog/search")
    }

    sealed class Message(routePath: String) : MobileRoute(routePath) {
        object Inbox : Message("message")
        data class Thread(val threadId: String) : Message("message/thread/$threadId")
    }

    sealed class Attachment(routePath: String) : MobileRoute(routePath) {
        object Root : Attachment("attachment")
        data class Detail(val attachmentId: String) : Attachment("attachment/$attachmentId")
    }

    sealed class Cart(routePath: String) : MobileRoute(routePath) {
        object Current : Cart("cart")
        object Checkout : Cart("cart/checkout")
        data class CheckoutResult(val checkoutId: String) : Cart("cart/checkout/result/$checkoutId")
    }

    sealed class Product(routePath: String) : MobileRoute(routePath) {
        object Listing : Product("vendor/retail")
        object New : Product("vendor/retail/new")
        data class Detail(val productId: String) : Product("vendor/retail/$productId")
    }

    sealed class Order(routePath: String) : MobileRoute(routePath) {
        object Listing : Order("vendor/order")
        object New : Order("vendor/order/new")
        data class Detail(val orderId: String) : Order("vendor/order/$orderId")
        data class Shipment(val orderId: String) : Order("vendor/order/$orderId/shipment")
        data class ShipmentDetail(val orderId: String, val shipmentId: String) : Order("vendor/order/$orderId/shipment/$shipmentId")
        data class Tax(val orderId: String) : Order("vendor/order/$orderId/tax")
        data class TaxDetail(val orderId: String, val taxId: String) : Order("vendor/order/$orderId/tax/$taxId")
    }

    sealed class Project(routePath: String) : MobileRoute(routePath) {
        object Listing : Project("vendor/project")
        object New : Project("vendor/project/new")
        data class Detail(val projectId: String) : Project("vendor/project/$projectId")
    }
}
