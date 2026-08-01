package app.mobiling.client.dashboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import app.mobiling.client.contract.order.OrderMobileItemPayload
import app.mobiling.client.contract.product.ProductMobileItemPayload
import app.mobiling.client.contract.project.ProjectMobileItemPayload
import app.mobiling.client.data.order.OrderGateway
import app.mobiling.client.data.product.ProductGateway
import app.mobiling.client.data.project.ProjectGateway
import org.junit.Rule
import org.junit.Test

/**
 * Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp.
 *
 * Behavioral coverage for the authenticated Android dashboard shell.
 */
class DashboardBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fallbackShellShowsDashboardAsRootWithPrimaryNavigation() {
        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = {},
            )
        }

        composeRule.onAllNodesWithText("Dashboard").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("Cart").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("Vendor").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("More").onFirst().assertIsDisplayed()
    }

    @Test
    fun vendorNavigationOpensVendorContextAndProfile() {
        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = {},
            )
        }

        composeRule.onAllNodesWithText("Vendor")
            .filter(hasClickAction())
            .onLast()
            .performClick()
        composeRule.onNodeWithText("My Vendor").fetchSemanticsNode()
        composeRule.onNodeWithText("My Profile").performScrollTo().performClick()
        composeRule.onAllNodesWithText("My Profile").onFirst().assertIsDisplayed()
    }

    @Test
    fun vendorAttachmentRouteOpensExistingAttachmentScreen() {
        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = {},
            )
        }

        composeRule.onAllNodesWithText("Vendor")
            .filter(hasClickAction())
            .onLast()
            .performClick()
        composeRule.onNodeWithText("My Attachment").performScrollTo().performClick()
        composeRule.onAllNodesWithText("Attachment").onFirst().assertIsDisplayed()
    }

    @Test
    fun vendorProductOrderAndProjectRoutesOpenProductScreens() {
        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = {},
            )
        }

        composeRule.onAllNodesWithText("Vendor").filter(hasClickAction()).onLast().performClick()
        composeRule.onNodeWithText("Products").performScrollTo().performClick()
        composeRule.onNodeWithText("Product gateway is not available.").assertIsDisplayed()

        composeRule.onAllNodesWithText("Vendor").filter(hasClickAction()).onLast().performClick()
        composeRule.onNodeWithText("Orders").performScrollTo().performClick()
        composeRule.onNodeWithText("Order gateway is not available.").assertIsDisplayed()

        composeRule.onAllNodesWithText("Vendor").filter(hasClickAction()).onLast().performClick()
        composeRule.onNodeWithText("Projects").performScrollTo().performClick()
        composeRule.onNodeWithText("Project gateway is not available.").assertIsDisplayed()
    }

    @Test
    fun productCrudLifecycleIsBehaviorallyComplete() {
        val gateway = ProductGatewayFixture()
        composeRule.setContent {
            DashboardMobileShell(null, productGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Products")
        composeRule.onNodeWithText("New Product").performClick()
        composeRule.onNodeWithText("Title *").performTextInput("Behavior Product")
        composeRule.onNodeWithText("Price").performTextInput("125.00")
        composeRule.onNodeWithText("Create Product").performClick()
        composeRule.waitUntil(5_000) { gateway.createCalls == 1 }
        composeRule.onNodeWithText("Behavior Product").assertIsDisplayed().performClick()

        composeRule.onAllNodesWithText("Behavior Product").filter(hasSetTextAction()).onFirst().performTextClearance()
        composeRule.onNodeWithText("Name").performTextInput("Updated Product")
        composeRule.onNodeWithText("Save changes").performClick()
        composeRule.waitUntil(5_000) { gateway.updateCalls == 1 }
        composeRule.onAllNodesWithText("Updated Product").onFirst().assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitUntil(5_000) { gateway.deleteCalls == 1 }
        composeRule.onAllNodesWithText("Updated Product").assertCountEquals(0)
        composeRule.onNodeWithText("No products yet.").assertIsDisplayed()
    }

    @Test
    fun orderCrudLifecycleIsBehaviorallyComplete() {
        val gateway = OrderGatewayFixture()
        composeRule.setContent {
            DashboardMobileShell(null, orderGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Orders")
        composeRule.onNodeWithText("New Order").performClick()
        composeRule.onNodeWithText("Reference *").performTextInput("ORDER-1001")
        composeRule.onNodeWithText("Total").performTextInput("240.50")
        composeRule.onNodeWithText("Create Order").performClick()
        composeRule.waitUntil(5_000) { gateway.createCalls == 1 }
        composeRule.onNodeWithText("ORDER-1001").assertIsDisplayed().performClick()

        composeRule.onAllNodesWithText("ORDER-1001").filter(hasSetTextAction()).onFirst().performTextClearance()
        composeRule.onNodeWithText("Reference").performTextInput("ORDER-UPDATED")
        composeRule.onNodeWithText("Save changes").performClick()
        composeRule.waitUntil(5_000) { gateway.updateCalls == 1 }
        composeRule.onAllNodesWithText("ORDER-UPDATED").onFirst().assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitUntil(5_000) { gateway.deleteCalls == 1 }
        composeRule.onAllNodesWithText("ORDER-UPDATED").assertCountEquals(0)
        composeRule.onNodeWithText("No orders yet.").assertIsDisplayed()
    }

    @Test
    fun projectCrudLifecycleIsBehaviorallyComplete() {
        val gateway = ProjectGatewayFixture()
        composeRule.setContent {
            DashboardMobileShell(null, projectGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Projects")
        composeRule.onNodeWithText("New Project").performClick()
        composeRule.onNodeWithText("Title *").performTextInput("Behavior Project")
        composeRule.onNodeWithText("Next").performScrollTo().performClick()
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, ".ProseMirror"))
            .perform(webClick())
            .perform(webKeys("Behavior project story"))
        composeRule.onNodeWithText("Next").performScrollTo().performClick()
        composeRule.onNodeWithText("Next").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Project").performScrollTo().performClick()
        composeRule.waitUntil(5_000) { gateway.createCalls == 1 }
        composeRule.onNodeWithText("Behavior Project").assertIsDisplayed().performClick()

        composeRule.onAllNodesWithText("Behavior Project").filter(hasSetTextAction()).onFirst().performTextClearance()
        composeRule.onNodeWithText("Name").performTextInput("Updated Project")
        composeRule.onNodeWithText("Save changes").performClick()
        composeRule.waitUntil(5_000) { gateway.updateCalls == 1 }
        composeRule.onAllNodesWithText("Updated Project").onFirst().assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitUntil(5_000) { gateway.deleteCalls == 1 }
        composeRule.onAllNodesWithText("Updated Project").assertCountEquals(0)
        composeRule.onNodeWithText("No projects yet.").assertIsDisplayed()
    }

    private fun openVendorRoute(route: String) {
        composeRule.onAllNodesWithText("Vendor").filter(hasClickAction()).onLast().performClick()
        composeRule.onNodeWithText(route).performScrollTo().performClick()
    }

    @Test
    fun productCreateRejectsMissingTitleAndInvalidPrice() {
        val gateway = ProductGatewayFixture()
        composeRule.setContent {
            DashboardMobileShell(null, productGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Products")
        composeRule.onNodeWithText("New Product").performClick()
        composeRule.onNodeWithText("Price").performTextInput("not-a-number")
        composeRule.onNodeWithText("Create Product").performClick()

        composeRule.onNodeWithText("Title is required.").assertIsDisplayed()
        composeRule.onNodeWithText("Price must be a number.").assertIsDisplayed()
        check(gateway.createCalls == 0)
    }

    @Test
    fun createFailureKeepsFormAndShowsGatewayError() {
        val gateway = ProductGatewayFixture(createFailure = "Product create rejected.")
        composeRule.setContent {
            DashboardMobileShell(null, productGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Products")
        composeRule.onNodeWithText("New Product").performClick()
        composeRule.onNodeWithText("Title *").performTextInput("Rejected Product")
        composeRule.onNodeWithText("Create Product").performClick()

        composeRule.onNodeWithText("Product create rejected.").assertIsDisplayed()
        composeRule.onNodeWithText("New Product").assertIsDisplayed()
        check(gateway.createCalls == 1)
    }

    @Test
    fun updateAndDeleteFailuresRemainOnDetailAndShowError() {
        val gateway = ProductGatewayFixture(
            initialRows = listOf(ProductMobileItemPayload("product-1", "Existing Product", "active", "\$10")),
            updateFailure = "Product update rejected.",
            deleteFailure = "Product delete rejected.",
        )
        composeRule.setContent {
            DashboardMobileShell(null, productGateway = gateway, vendorId = "test-vendor", onSignOut = {})
        }

        openVendorRoute("Products")
        composeRule.onNodeWithText("Existing Product").performClick()
        composeRule.onAllNodesWithText("Existing Product").filter(hasSetTextAction()).onFirst().performTextClearance()
        composeRule.onNodeWithText("Name").performTextInput("Rejected Update")
        composeRule.onNodeWithText("Save changes").performClick()
        composeRule.onNodeWithText("Product update rejected.").assertIsDisplayed()
        composeRule.onNodeWithText("Product Detail").assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Product delete rejected.").assertIsDisplayed()
        composeRule.onNodeWithText("Product Detail").assertIsDisplayed()
        check(gateway.updateCalls == 1)
        check(gateway.deleteCalls == 1)
    }

    @Test
    fun moreShowsInactiveCatalogAndMessageAsComingSoon() {
        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = {},
            )
        }

        composeRule.onAllNodesWithText("More")
            .filter(hasClickAction())
            .onLast()
            .performClick()
        composeRule.onNodeWithText("Catalog").assertIsDisplayed()
        composeRule.onNodeWithText("Message").assertIsDisplayed()
        composeRule.onAllNodesWithText("Coming soon", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun accountSignOutInvokesAuthenticatedBoundary() {
        var signOutCalls = 0

        composeRule.setContent {
            DashboardMobileShell(
                navigationShellGateway = null,
                vendorId = "test-vendor",
                onSignOut = { signOutCalls += 1 },
            )
        }

        composeRule.onNodeWithContentDescription("Account").performClick()
        composeRule.onNodeWithText("Sign Out").assertIsDisplayed()
        composeRule.onNodeWithText("Sign Out").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { signOutCalls == 1 }
    }
}

private class ProductGatewayFixture(
    initialRows: List<ProductMobileItemPayload> = emptyList(),
    private val createFailure: String? = null,
    private val updateFailure: String? = null,
    private val deleteFailure: String? = null,
) : ProductGateway {
    private val rows = initialRows.toMutableList()
    var createCalls = 0
    var updateCalls = 0
    var deleteCalls = 0

    override suspend fun loadProducts(vendorId: String) = rows.toList()
    override suspend fun createProduct(fields: Map<String, String>) {
        createCalls++
        createFailure?.let { error(it) }
        rows += ProductMobileItemPayload("product-1", fields.getValue("title"), fields["status"], fields["price"])
    }
    override suspend fun updateProduct(productId: String, fields: Map<String, String>) {
        updateCalls++
        updateFailure?.let { error(it) }
        val index = rows.indexOfFirst { it.productId == productId }
        rows[index] = rows[index].copy(title = fields["title"] ?: rows[index].title)
    }
    override suspend fun deleteProduct(productId: String) {
        deleteCalls++
        deleteFailure?.let { error(it) }
        rows.removeAll { it.productId == productId }
    }
}

private class OrderGatewayFixture : OrderGateway {
    private val rows = mutableListOf<OrderMobileItemPayload>()
    var createCalls = 0
    var updateCalls = 0
    var deleteCalls = 0

    override suspend fun loadOrders(vendorId: String) = rows.toList()
    override suspend fun createOrder(fields: Map<String, String>) {
        createCalls++
        rows += OrderMobileItemPayload("order-1", fields.getValue("reference"), fields["status"], fields["total"])
    }
    override suspend fun updateOrder(orderId: String, fields: Map<String, String>) {
        updateCalls++
        val index = rows.indexOfFirst { it.orderId == orderId }
        rows[index] = rows[index].copy(reference = fields["reference"] ?: rows[index].reference)
    }
    override suspend fun deleteOrder(orderId: String) {
        deleteCalls++
        rows.removeAll { it.orderId == orderId }
    }
}

private class ProjectGatewayFixture : ProjectGateway {
    private val rows = mutableListOf<ProjectMobileItemPayload>()
    var createCalls = 0
    var updateCalls = 0
    var deleteCalls = 0

    override suspend fun loadProjects(vendorId: String) = rows.toList()
    override suspend fun createProject(fields: Map<String, String>) {
        createCalls++
        rows += ProjectMobileItemPayload("project-1", fields.getValue("title"), fields["status"], fields["location"])
    }
    override suspend fun updateProject(projectId: String, fields: Map<String, String>) {
        updateCalls++
        val index = rows.indexOfFirst { it.projectId == projectId }
        rows[index] = rows[index].copy(title = fields["title"] ?: rows[index].title)
    }
    override suspend fun deleteProject(projectId: String) {
        deleteCalls++
        rows.removeAll { it.projectId == projectId }
    }
}
