package app.mobiling.client

import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.catalog.CatalogFeatureBridge
import app.mobiling.client.data.attachment.AttachmentHttpGateway
import app.mobiling.client.data.auth.session.AccessHttpAuthSessionGateway
import app.mobiling.client.data.cart.CartHttpGateway
import app.mobiling.client.data.catalog.CatalogHttpGateway
import app.mobiling.client.data.navigation.shell.NavigationHttpShellGateway
import app.mobiling.client.data.message.thread.MessageHttpThreadGateway
import app.mobiling.client.data.notification.NotificationHttpGateway
import app.mobiling.client.data.order.OrderHttpGateway
import app.mobiling.client.data.product.ProductHttpGateway
import app.mobiling.client.data.project.ProjectHttpGateway
import app.mobiling.client.data.vendor.payout.VendorHttpPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorHttpProfileGateway
import app.mobiling.client.data.vendor.statement.VendorHttpStatementGateway
import app.mobiling.client.data.vendor.summary.VendorHttpSummaryGateway
import app.mobiling.client.data.vendor.transaction.VendorHttpTransactionGateway
import app.mobiling.client.data.wallet.WalletHttpGateway
import app.mobiling.client.message.MessageFeatureBridge
import app.mobiling.client.notification.NotificationFeatureBridge
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class MobileApplicationGraph private constructor(val composition: MobileApplicationComposition) {
    private val baseUrl = composition.mobileEdgeBaseUrl
    private val sessionCookies = mutableListOf<Cookie>()
    private val httpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            @Synchronized
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val now = System.currentTimeMillis()
                sessionCookies.removeAll { existing ->
                    existing.expiresAt <= now || cookies.any { incoming ->
                        existing.name == incoming.name &&
                            existing.domain == incoming.domain &&
                            existing.path == incoming.path
                    }
                }
                sessionCookies += cookies.filter { cookie -> cookie.expiresAt > now }
            }

            @Synchronized
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val now = System.currentTimeMillis()
                sessionCookies.removeAll { cookie -> cookie.expiresAt <= now }
                return sessionCookies.filter { cookie -> cookie.matches(url) }
            }
        })
        .build()
    private val cartGateway = CartHttpGateway(baseUrl, httpClient)
    private val attachmentGateway = AttachmentHttpGateway(baseUrl, httpClient)
    private val catalogGateway = CatalogHttpGateway(
        baseUrl,
        composition.configuration.catalog.primaryCatalog,
        httpClient,
    )

    val accessAuthFeatureBridge = AccessAuthFeatureBridge(AccessHttpAuthSessionGateway(baseUrl, httpClient))
    val cartFeatureBridge = CartFeatureBridge(cartGateway, cartGateway, cartGateway)
    val attachmentFeatureBridge = AttachmentFeatureBridge(attachmentGateway, attachmentGateway)
    val catalogFeatureBridge = CatalogFeatureBridge(catalogGateway, catalogGateway)
    val navigationShellGateway = NavigationHttpShellGateway(baseUrl, httpClient)
    val messageFeatureBridge = MessageFeatureBridge(MessageHttpThreadGateway(baseUrl, httpClient))
    val notificationFeatureBridge = NotificationFeatureBridge(NotificationHttpGateway(baseUrl, httpClient))
    val productGateway = ProductHttpGateway(baseUrl, httpClient)
    val orderGateway = OrderHttpGateway(baseUrl, httpClient)
    val projectGateway = ProjectHttpGateway(baseUrl, httpClient)
    val vendorProfileGateway = VendorHttpProfileGateway(baseUrl, httpClient)
    val vendorSummaryGateway = VendorHttpSummaryGateway(baseUrl, httpClient)
    val vendorStatementGateway = VendorHttpStatementGateway(baseUrl, httpClient)
    val vendorPayoutGateway = VendorHttpPayoutGateway(baseUrl, httpClient)
    val vendorTransactionGateway = VendorHttpTransactionGateway(baseUrl, httpClient)
    val walletGateway = WalletHttpGateway(baseUrl, httpClient)

    companion object {
        fun current(composition: MobileApplicationComposition = MobileApplicationComposer.current()) =
            MobileApplicationGraph(composition)

        fun current(localText: Map<String, String>): MobileApplicationGraph =
            MobileApplicationGraph(MobileApplicationComposer.current(localText))
    }
}
