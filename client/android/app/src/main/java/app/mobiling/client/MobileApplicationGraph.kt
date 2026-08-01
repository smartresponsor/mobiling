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
import app.mobiling.client.data.order.OrderHttpGateway
import app.mobiling.client.data.product.ProductHttpGateway
import app.mobiling.client.data.project.ProjectHttpGateway
import app.mobiling.client.data.vendor.payout.VendorHttpPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorHttpProfileGateway
import app.mobiling.client.data.vendor.statement.VendorHttpStatementGateway
import app.mobiling.client.data.vendor.summary.VendorHttpSummaryGateway
import app.mobiling.client.data.vendor.transaction.VendorHttpTransactionGateway

class MobileApplicationGraph private constructor(val composition: MobileApplicationComposition) {
    private val baseUrl = composition.mobileEdgeBaseUrl
    private val cartGateway = CartHttpGateway(baseUrl)
    private val attachmentGateway = AttachmentHttpGateway(baseUrl)
    private val catalogGateway = CatalogHttpGateway(
        baseUrl,
        composition.configuration.catalog.primaryCatalog,
    )

    val accessAuthFeatureBridge = AccessAuthFeatureBridge(AccessHttpAuthSessionGateway(baseUrl))
    val cartFeatureBridge = CartFeatureBridge(cartGateway, cartGateway, cartGateway)
    val attachmentFeatureBridge = AttachmentFeatureBridge(attachmentGateway, attachmentGateway)
    val catalogFeatureBridge = CatalogFeatureBridge(catalogGateway, catalogGateway)
    val navigationShellGateway = NavigationHttpShellGateway(baseUrl)
    val productGateway = ProductHttpGateway(baseUrl)
    val orderGateway = OrderHttpGateway(baseUrl)
    val projectGateway = ProjectHttpGateway(baseUrl)
    val vendorProfileGateway = VendorHttpProfileGateway(baseUrl)
    val vendorSummaryGateway = VendorHttpSummaryGateway(baseUrl)
    val vendorStatementGateway = VendorHttpStatementGateway(baseUrl)
    val vendorPayoutGateway = VendorHttpPayoutGateway(baseUrl)
    val vendorTransactionGateway = VendorHttpTransactionGateway(baseUrl)

    companion object {
        fun current(composition: MobileApplicationComposition = MobileApplicationComposer.current()) =
            MobileApplicationGraph(composition)

        fun current(localText: Map<String, String>): MobileApplicationGraph =
            MobileApplicationGraph(MobileApplicationComposer.current(localText))
    }
}
