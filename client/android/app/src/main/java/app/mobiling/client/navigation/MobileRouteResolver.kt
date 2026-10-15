package app.mobiling.client.navigation

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
object MobileRouteResolver {
    fun resolve(rawRoute: String?, query: Map<String, String> = emptyMap()): MobileRoute? {
        val route = normalizeRoute(rawRoute)
        val segments = route.split('/').filter { it.isNotBlank() }

        return when {
            route == "dashboard" -> MobileRoute.Dashboard
            route == "more" -> MobileRoute.More
            route == "money" -> MobileRoute.Money
            route == "access/sign-in" -> MobileRoute.Access.SignIn
            route == "access/register" -> MobileRoute.Access.Register
            route == "access/recovery/request" -> MobileRoute.Access.RecoveryRequest
            route == "access/recovery/reset" -> MobileRoute.Access.RecoveryReset
            route == "access/verification" -> MobileRoute.Access.Verification
            route == "access/password" -> MobileRoute.Access.Password
            route == "access/sign-out" -> MobileRoute.Access.SignOut
            route == "wallet" -> MobileRoute.Wallet.Overview
            route == "wallet/transaction" -> MobileRoute.Wallet.Transaction
            route == "wallet/funding" -> MobileRoute.Wallet.Funding
            route == "wallet/withdrawal" -> MobileRoute.Wallet.Withdrawal
            segments.size == 3 && segments[0] == "wallet" && segments[1] == "withdrawal" -> MobileRoute.Wallet.WithdrawalDetail(segments[2])
            route == "vendor" -> MobileRoute.Vendor.Overview
            route == "vendor/page" -> MobileRoute.Vendor.Profile
            route == "vendor/summary" -> MobileRoute.Vendor.Summary
            route == "vendor/statement" -> MobileRoute.Vendor.Statement
            route == "vendor/payout" -> MobileRoute.Vendor.Payout
            route == "vendor/transaction" -> MobileRoute.Vendor.Transaction
            route == "vendor/attachment" -> MobileRoute.Vendor.Attachment
            route == "catalog" -> MobileRoute.Catalog.Root
            route == "catalog/browse" -> MobileRoute.Catalog.Browse
            route == "catalog/search" -> MobileRoute.Catalog.Search(query["q"] ?: query["searchText"])
            segments.size == 3 && segments[0] == "catalog" && segments[1] == "node" -> MobileRoute.Catalog.Node(segments[2])
            route == "message" -> MobileRoute.Message.Inbox
            segments.size == 3 && segments[0] == "message" && segments[1] == "thread" -> MobileRoute.Message.Thread(segments[2])
            route == "notification" -> MobileRoute.Notification
            route == "support" -> MobileRoute.Support.Home
            route == "support/case" -> MobileRoute.Support.Cases
            segments.size == 3 && segments[0] == "support" && segments[1] == "case" -> MobileRoute.Support.CaseDetail(segments[2])
            route.startsWith("support/") -> MobileRoute.Support.Flow(route)
            route == "attachment" -> MobileRoute.Attachment.Root
            segments.size == 2 && segments[0] == "attachment" -> MobileRoute.Attachment.Detail(segments[1])
            route == "cart" -> MobileRoute.Cart.Current
            route == "cart/checkout" -> MobileRoute.Cart.Checkout
            segments.size == 4 && segments[0] == "cart" && segments[1] == "checkout" && segments[2] == "result" -> MobileRoute.Cart.CheckoutResult(segments[3])
            route == "vendor/retail" -> MobileRoute.Product.Listing
            route == "vendor/retail/new" -> MobileRoute.Product.New
            segments.size == 4 && segments[0] == "vendor" && segments[1] == "retail" && segments[3] == "placement" -> MobileRoute.Product.Placement(segments[2])
            segments.size == 3 && segments[0] == "vendor" && segments[1] == "product" -> MobileRoute.Product.Detail(segments[2])
            route == "vendor/order" -> MobileRoute.Order.Listing
            route == "vendor/order/new" -> MobileRoute.Order.New
            segments.size == 3 && segments[0] == "vendor" && segments[1] == "order" -> MobileRoute.Order.Detail(segments[2])
            segments.size == 4 && segments[0] == "vendor" && segments[1] == "order" && segments[3] == "shipment" -> MobileRoute.Order.Shipment(segments[2])
            segments.size == 5 && segments[0] == "vendor" && segments[1] == "order" && segments[3] == "shipment" -> MobileRoute.Order.ShipmentDetail(segments[2], segments[4])
            segments.size == 4 && segments[0] == "vendor" && segments[1] == "order" && segments[3] == "tax" -> MobileRoute.Order.Tax(segments[2])
            segments.size == 5 && segments[0] == "vendor" && segments[1] == "order" && segments[3] == "tax" -> MobileRoute.Order.TaxDetail(segments[2], segments[4])
            route == "vendor/project" -> MobileRoute.Project.Listing
            route == "vendor/project/new" -> MobileRoute.Project.New
            segments.size == 3 && segments[0] == "vendor" && segments[1] == "project" -> MobileRoute.Project.Detail(segments[2])
            else -> null
        }
    }

    fun resolve(link: MobileLink): MobileRoute? = resolve(link.route, link.query)

    fun isKnownRoute(rawRoute: String?): Boolean = resolve(rawRoute) != null

    fun isSignOutAction(action: String?, rawRoute: String?): Boolean =
        action == "access.sign_out" || resolve(rawRoute) == MobileRoute.Access.SignOut

    fun isCurrentlyRenderable(rawRoute: String?): Boolean = when (resolve(rawRoute)) {
        MobileRoute.Dashboard,
        MobileRoute.More,
        MobileRoute.Money,
        MobileRoute.Wallet.Overview,
        MobileRoute.Wallet.Transaction,
        MobileRoute.Wallet.Funding,
        MobileRoute.Wallet.Withdrawal,
        is MobileRoute.Wallet.WithdrawalDetail,
        MobileRoute.Cart.Current,
        MobileRoute.Vendor.Overview,
        MobileRoute.Vendor.Profile,
        MobileRoute.Vendor.Summary,
        MobileRoute.Vendor.Statement,
        MobileRoute.Vendor.Payout,
        MobileRoute.Vendor.Transaction,
        MobileRoute.Vendor.Attachment,
        MobileRoute.Message.Inbox,
        MobileRoute.Notification,
        MobileRoute.Support.Home,
        MobileRoute.Support.Cases,
        is MobileRoute.Support.CaseDetail,
        is MobileRoute.Support.Flow,
        MobileRoute.Product.Listing,
        MobileRoute.Product.New,
        is MobileRoute.Product.Placement,
        is MobileRoute.Product.Detail,
        MobileRoute.Order.Listing,
        MobileRoute.Order.New,
        is MobileRoute.Order.Detail,
        is MobileRoute.Order.Shipment,
        is MobileRoute.Order.ShipmentDetail,
        is MobileRoute.Order.Tax,
        is MobileRoute.Order.TaxDetail,
        MobileRoute.Project.Listing,
        MobileRoute.Project.New,
        is MobileRoute.Project.Detail,
        MobileRoute.Catalog.Root,
        MobileRoute.Attachment.Root -> true
        else -> false
    }

    fun normalizeRoute(rawRoute: String?): String {
        val normalized = rawRoute
            ?.trim()
            .orEmpty()
            .trim('/')
            .replace(Regex("/{2,}"), "/")

        return if (normalized == "vendor/profile") "vendor/page" else normalized
    }
}
