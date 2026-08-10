package app.mobiling.client.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.RetailKind
import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.attachment.AttachmentMobileScreen
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.catalog.CatalogFeatureBridge
import app.mobiling.client.catalog.CatalogMobileScreen
import app.mobiling.client.cart.CartMobileScreen
import app.mobiling.client.contract.navigation.shell.NavigationMobileItemPayload
import app.mobiling.client.data.navigation.shell.NavigationShellGateway
import app.mobiling.client.data.order.OrderGateway
import app.mobiling.client.data.product.ProductGateway
import app.mobiling.client.data.project.ProjectGateway
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import app.mobiling.client.data.vendor.statement.VendorStatementGateway
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import app.mobiling.client.data.vendor.transaction.VendorTransactionGateway
import app.mobiling.client.ui.navigation.shell.NavigationMobileShellScreenContract
import app.mobiling.client.navigation.MobileRouteResolver
import app.mobiling.client.usecase.navigation.shell.NavigationLoadShellUseCase
import app.mobiling.client.vendor.VendorMobileOverviewScreen
import app.mobiling.client.vendor.VendorMobilePayoutScreen
import app.mobiling.client.vendor.VendorMobileProfileScreen
import app.mobiling.client.vendor.VendorMobileStatementScreen
import app.mobiling.client.vendor.VendorMobileSummaryScreen
import app.mobiling.client.vendor.VendorMobileTransactionScreen
import app.mobiling.client.vendor.ProductMobileScreen
import app.mobiling.client.vendor.OrderMobileScreen
import app.mobiling.client.vendor.ProjectMobileScreen
import app.mobiling.client.vendor.VendorNewMobileScreen
import app.mobiling.client.vendor.RetailNewFields
import app.mobiling.client.vendor.OrderNewFields
import app.mobiling.client.vendor.ProjectNewMobileScreen
import app.mobiling.client.message.MessageFeatureBridge
import app.mobiling.client.message.MessageMobileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMobileShell(
    navigationShellGateway: NavigationShellGateway?,
    messageFeatureBridge: MessageFeatureBridge? = null,
    productGateway: ProductGateway? = null,
    orderGateway: OrderGateway? = null,
    projectGateway: ProjectGateway? = null,
    cartFeatureBridge: CartFeatureBridge? = null,
    catalogFeatureBridge: CatalogFeatureBridge? = null,
    attachmentFeatureBridge: AttachmentFeatureBridge? = null,
    vendorId: String? = null,
    vendorProfileGateway: VendorProfileGateway? = null,
    vendorSummaryGateway: VendorSummaryGateway? = null,
    vendorStatementGateway: VendorStatementGateway? = null,
    vendorPayoutGateway: VendorPayoutGateway? = null,
    vendorTransactionGateway: VendorTransactionGateway? = null,
    initialRoute: String = "vendor/project",
    catalogEnabled: Boolean = true,
    availableRetailKinds: List<RetailKind> = RetailKind.entries,
    navigationLabelResolver: (route: String?, key: String, backendLabel: String) -> String = { _, _, label -> label },
    onSignOut: () -> Unit,
) {
    var selectedRoute by remember { mutableStateOf(initialRoute) }
    var navigationOpen by remember { mutableStateOf(false) }
    var accountOpen by remember { mutableStateOf(false) }
    var newChooserOpen by remember { mutableStateOf(false) }
    var selectedProductKind by remember(availableRetailKinds) { mutableStateOf(availableRetailKinds.first().code) }
    var shell by remember { mutableStateOf(localizeShell(fallbackShell(), navigationLabelResolver)) }

    LaunchedEffect(navigationShellGateway) {
        if (navigationShellGateway != null) {
            shell = try {
                localizeShell(
                    NavigationMobileShellScreenContract.from(NavigationLoadShellUseCase(navigationShellGateway).invoke()),
                    navigationLabelResolver,
                )
            } catch (_: Exception) {
                localizeShell(fallbackShell(), navigationLabelResolver)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navigationOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation")
                    }
                },
                title = {
                    Text(
                        text = routeTitle(selectedRoute, selectedProductKind),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = { accountOpen = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                shell.bottomPrimary.filter { it.visible }.forEach { item ->
                    NavigationBarItem(
                        selected = selectedRoute == item.route,
                        onClick = {
                            if (item.enabled && isHandledRoute(item.route) && (item.route != "catalog" || catalogEnabled)) {
                                selectedRoute = item.route ?: item.key
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = iconFor(item),
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                        enabled = item.enabled,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (!selectedRoute.endsWith("/new")) {
                FloatingActionButton(onClick = { newChooserOpen = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create new")
                }
            }
        },
    ) { padding ->
        DashboardContent(
            selectedRoute = selectedRoute,
            selectedProductKind = selectedProductKind,
            availableRetailKinds = availableRetailKinds,
            shell = shell,
            messageFeatureBridge = messageFeatureBridge,
            productGateway = productGateway,
            orderGateway = orderGateway,
            projectGateway = projectGateway,
            cartFeatureBridge = cartFeatureBridge,
            catalogFeatureBridge = catalogFeatureBridge,
            attachmentFeatureBridge = attachmentFeatureBridge,
            vendorId = vendorId,
            vendorProfileGateway = vendorProfileGateway,
            vendorSummaryGateway = vendorSummaryGateway,
            vendorStatementGateway = vendorStatementGateway,
            vendorPayoutGateway = vendorPayoutGateway,
            vendorTransactionGateway = vendorTransactionGateway,
            onRouteSelected = { route ->
                val normalizedRoute = MobileRouteResolver.normalizeRoute(route)
                if (isHandledRoute(normalizedRoute)) selectedRoute = normalizedRoute
            },
            padding = padding,
        )
    }

    if (navigationOpen) {
        ModalBottomSheet(onDismissRequest = { navigationOpen = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                ShellSection(
                    title = "Navigation",
                    items = shell.moreDrawer,
                    navigationLabelResolver = navigationLabelResolver,
                    onItemClick = { item ->
                        if (item.enabled && isHandledRoute(item.route) && (item.route != "catalog" || catalogEnabled)) {
                            selectedRoute = MobileRouteResolver.normalizeRoute(item.route)
                        }
                        navigationOpen = false
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (accountOpen) {
        ModalBottomSheet(onDismissRequest = { accountOpen = false }) {
            ShellSection(
                title = "Account",
                items = shell.accountQuick,
                onItemClick = { item ->
                    when {
                        MobileRouteResolver.isSignOutAction(item.action, item.route) -> onSignOut()
                        item.enabled && isHandledRoute(item.route) && (item.route != "catalog" || catalogEnabled) -> selectedRoute = MobileRouteResolver.normalizeRoute(item.route)
                        else -> Unit
                    }
                    accountOpen = false
                },
            )
        }
    }

    if (newChooserOpen) {
        ModalBottomSheet(onDismissRequest = { newChooserOpen = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                availableRetailKinds.forEach { kind ->
                    NewChoice(
                        title = retailKindLabel(kind.code),
                        description = retailKindDescription(kind),
                        icon = retailKindIcon(kind),
                    ) {
                        selectedProductKind = kind.code
                        selectedRoute = "vendor/retail/new"
                        newChooserOpen = false
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardContent(
    selectedRoute: String,
    selectedProductKind: String,
    availableRetailKinds: List<RetailKind>,
    shell: NavigationMobileShellScreenContract,
    messageFeatureBridge: MessageFeatureBridge?,
    productGateway: ProductGateway?,
    orderGateway: OrderGateway?,
    projectGateway: ProjectGateway?,
    cartFeatureBridge: CartFeatureBridge?,
    catalogFeatureBridge: CatalogFeatureBridge?,
    attachmentFeatureBridge: AttachmentFeatureBridge?,
    vendorId: String?,
    vendorProfileGateway: VendorProfileGateway?,
    vendorSummaryGateway: VendorSummaryGateway?,
    vendorStatementGateway: VendorStatementGateway?,
    vendorPayoutGateway: VendorPayoutGateway?,
    vendorTransactionGateway: VendorTransactionGateway?,
    onRouteSelected: (String) -> Unit,
    padding: PaddingValues,
) {
    when (selectedRoute) {
        "attachment", "vendor/attachment" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                AttachmentMobileScreen(vendorId = vendorId, attachmentFeatureBridge = attachmentFeatureBridge)
            }
            return
        }
        "cart" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                CartMobileScreen(cartFeatureBridge = cartFeatureBridge)
            }
            return
        }
        "catalog" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                CatalogMobileScreen(catalogFeatureBridge = catalogFeatureBridge)
            }
            return
        }
        "message" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                MessageMobileScreen(messageFeatureBridge = messageFeatureBridge)
            }
            return
        }
        "vendor" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobileOverviewScreen(
                    vendorId = vendorId,
                    vendorSummaryGateway = vendorSummaryGateway,
                    onRouteSelected = onRouteSelected,
                )
            }
            return
        }
        "vendor/page" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobileProfileScreen(vendorId = vendorId, vendorProfileGateway = vendorProfileGateway, attachmentFeatureBridge = attachmentFeatureBridge)
            }
            return
        }
        "vendor/summary" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobileSummaryScreen(vendorId = vendorId, vendorSummaryGateway = vendorSummaryGateway)
            }
            return
        }
        "vendor/statement" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobileStatementScreen(vendorId = vendorId, vendorStatementGateway = vendorStatementGateway)
            }
            return
        }
        "vendor/payout" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobilePayoutScreen(vendorId = vendorId, vendorPayoutGateway = vendorPayoutGateway)
            }
            return
        }
        "vendor/transaction" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                VendorMobileTransactionScreen(vendorId = vendorId, vendorTransactionGateway = vendorTransactionGateway)
            }
            return
        }
        "vendor/retail" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                ProductMobileScreen(vendorId, null, productGateway, onRouteSelected)
            }
            return
        }
        "vendor/order" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                OrderMobileScreen(vendorId, null, orderGateway, onRouteSelected)
            }
            return
        }
        "vendor/project" -> {
            Box(Modifier.fillMaxSize().padding(padding)) {
                ProjectMobileScreen(vendorId, null, projectGateway, onRouteSelected)
            }
            return
        }
        else -> {
            val segments = selectedRoute.split('/').filter(String::isNotBlank)
            when {
                selectedRoute == "vendor/retail/new" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        VendorNewMobileScreen(
                            singular = retailKindLabel(selectedProductKind),
                            listRoute = "vendor/retail",
                            fields = RetailNewFields,
                            onCreate = { fields -> productGateway?.createProduct(fields) ?: error("Retail gateway is not available.") },
                            onRouteSelected = onRouteSelected,
                            initialValues = mapOf(
                                "kind" to selectedProductKind,
                                "currency" to "USD",
                            ),
                            availableRetailKinds = availableRetailKinds,
                        )
                    }
                    return
                }
                selectedRoute == "vendor/order/new" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        VendorNewMobileScreen("Order", "vendor/order", OrderNewFields, { fields -> orderGateway?.createOrder(fields) ?: error("Order gateway is not available.") }, onRouteSelected)
                    }
                    return
                }
                selectedRoute == "vendor/project/new" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        ProjectNewMobileScreen({ fields -> projectGateway?.createProject(fields) ?: error("Project gateway is not available.") }, onRouteSelected)
                    }
                    return
                }
                segments.size == 3 && segments[0] == "vendor" && segments[1] == "product" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) { ProductMobileScreen(vendorId, segments[2], productGateway, onRouteSelected) }
                    return
                }
                segments.size >= 3 && segments[0] == "vendor" && segments[1] == "order" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) { OrderMobileScreen(vendorId, segments[2], orderGateway, onRouteSelected) }
                    return
                }
                segments.size == 3 && segments[0] == "vendor" && segments[1] == "project" -> {
                    Box(Modifier.fillMaxSize().padding(padding)) { ProjectMobileScreen(vendorId, segments[2], projectGateway, onRouteSelected) }
                    return
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (selectedRoute) {
            "attachment" -> item {
                AttachmentMobileScreen(vendorId = vendorId, attachmentFeatureBridge = attachmentFeatureBridge)
            }
            "cart" -> item {
                CartMobileScreen(cartFeatureBridge = cartFeatureBridge)
            }
            "catalog" -> item {
                CatalogMobileScreen(catalogFeatureBridge = catalogFeatureBridge)
            }
            "vendor" -> item {
                ShellSection(title = "Vendor", items = shell.vendorContext, onItemClick = { item ->
                    item.route?.let(onRouteSelected)
                })
            }
            "vendor/page" -> item {
                VendorMobileProfileScreen(vendorId = vendorId, vendorProfileGateway = vendorProfileGateway, attachmentFeatureBridge = attachmentFeatureBridge)
            }
            "vendor/summary" -> item {
                VendorMobileSummaryScreen(vendorId = vendorId, vendorSummaryGateway = vendorSummaryGateway)
            }
            "vendor/statement" -> item {
                VendorMobileStatementScreen(vendorId = vendorId, vendorStatementGateway = vendorStatementGateway)
            }
            "vendor/payout" -> item {
                VendorMobilePayoutScreen(vendorId = vendorId, vendorPayoutGateway = vendorPayoutGateway)
            }
            "vendor/transaction" -> item {
                VendorMobileTransactionScreen(vendorId = vendorId, vendorTransactionGateway = vendorTransactionGateway)
            }
            "notification" -> item {
                EmptyMobileState(title = "Notifications", description = "Important 1Tasker updates will appear here.")
            }
            "more" -> item {
                ShellSection(title = "More", items = shell.moreDrawer, onItemClick = { item ->
                    item.route?.let(onRouteSelected)
                })
            }
            else -> item {
                ShellSection(title = "Primary", items = shell.bottomPrimary)
            }
        }
    }
}

@Composable
private fun NewChoice(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp).size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyMobileState(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShellSection(
    title: String,
    items: List<NavigationMobileItemPayload>,
    navigationLabelResolver: (route: String?, key: String, backendLabel: String) -> String = { _, _, label -> label },
    onItemClick: (NavigationMobileItemPayload) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.filter { it.visible }.forEach { item ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = item.enabled) { onItemClick(item) },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (item.enabled) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = iconFor(item),
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!item.enabled) {
                        AssistChip(onClick = {}, label = { Text(item.badge ?: "Coming soon") })
                    }
                }
            }
        }
    }
}

private fun iconFor(item: NavigationMobileItemPayload): ImageVector = when (item.icon) {
    "cart" -> Icons.Default.ShoppingCart
    "store" -> Icons.Default.Storefront
    "person" -> Icons.Default.Person
    "attachment" -> Icons.Default.AttachFile
    "message" -> Icons.Default.ChatBubbleOutline
    "catalog" -> Icons.Default.Inventory2
    "key" -> Icons.Default.VpnKey
    "logout" -> Icons.Default.Logout
    "summary" -> Icons.Default.Dashboard
    "statement" -> Icons.Default.ReceiptLong
    "payout" -> Icons.Default.Payments
    "receipt" -> Icons.Default.ReceiptLong
    "menu" -> Icons.Default.MoreHoriz
    "tasks" -> Icons.Default.ReceiptLong
    "notification" -> Icons.Default.Notifications
    else -> Icons.Default.Dashboard
}

private fun itemDescription(item: NavigationMobileItemPayload): String = when (item.route) {
    "dashboard" -> "Overview of your active workspace."
    "cart" -> "Review selected products and checkout activity."
    "vendor" -> "Open vendor tools and business information."
    "vendor/page" -> "Review your 1Tasker profile identity and completion."
    "vendor/summary" -> "See the current vendor status at a glance."
    "vendor/statement" -> "Review statement totals and status."
    "vendor/payout" -> "Track available and pending payout amounts."
    "vendor/transaction" -> "Review recent vendor transactions."
    "attachment" -> "Manage files linked to your vendor workspace."
    "catalog" -> "Browse and manage catalog capabilities."
    "vendor/project" -> "Review active tasks and jobs."
    "vendor/retail" -> "Manage your published services."
    "message" -> "Open task and customer conversations."
    "notification" -> "Review important 1Tasker updates."
    else -> item.badge ?: item.route ?: item.key
}

private fun fallbackShell(): NavigationMobileShellScreenContract = NavigationMobileShellScreenContract(
    bottomPrimary = listOf(
        item("tasks", "Tasks", "tasks", true, "vendor/project"),
        item("message", "Messages", "message", true, "message"),
        item("services", "Services", "store", true, "vendor/retail"),
        item("notification", "Notifications", "notification", true, "notification"),
        item("profile", "Profile", "person", true, "vendor/page"),
    ),
    accountQuick = listOf(
        item("vendor_page", "Profile", "person", true, "vendor/page"),
        item("access_password", "Change Password", "key", false, "access/password"),
        item("access_verification", "Verification", "key", false, "access/verification"),
        item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
        item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action = "access.sign_out"),
    ),
    moreDrawer = listOf(
        item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
        item("tasks", "Tasks", "tasks", true, "vendor/project"),
        item("message", "Messages", "message", true, "message"),
        item("services", "Services", "store", true, "vendor/retail"),
        item("notification", "Notifications", "notification", true, "notification"),
        item("vendor_page", "Profile", "person", true, "vendor/page"),
        item("catalog", "Catalog", "catalog", false, "catalog"),
        item("attachment", "Attachment", "attachment", true, "attachment"),
    ),
    vendorContext = listOf(
        item("vendor_overview", "Profile Overview", "person", true, "vendor"),
        item("vendor_page", "Profile", "person", true, "vendor/page"),
        item("vendor_summary", "Summary", "summary", true, "vendor/summary"),
        item("vendor_statement", "Statement", "statement", true, "vendor/statement"),
        item("vendor_payout", "Payout", "payout", true, "vendor/payout"),
        item("vendor_transaction", "Transaction", "receipt", true, "vendor/transaction"),
        item("vendor_attachment", "My Attachment", "attachment", true, "attachment"),
        item("vendor_product", "Services", "catalog", true, "vendor/retail"),
        item("vendor_order", "Orders", "statement", true, "vendor/order"),
        item("vendor_project", "Tasks", "tasks", true, "vendor/project"),
    ),
)


private fun item(
    key: String,
    label: String,
    icon: String,
    enabled: Boolean,
    route: String,
    action: String? = null,
): NavigationMobileItemPayload = NavigationMobileItemPayload(
    key = key,
    label = label,
    icon = icon,
    badge = if (enabled) null else "Coming soon",
    enabled = enabled,
    visible = true,
    status = if (enabled) "active" else "coming_soon",
    disabledReason = if (enabled) null else "component_disabled",
    requiredComponent = null,
    location = "mobile",
    group = "fallback",
    groupLabel = "Fallback",
    action = action,
    route = route,
)

private fun localizeShell(
    shell: NavigationMobileShellScreenContract,
    resolver: (route: String?, key: String, backendLabel: String) -> String,
): NavigationMobileShellScreenContract {
    fun localize(items: List<NavigationMobileItemPayload>): List<NavigationMobileItemPayload> =
        items.map { item ->
            val route = MobileRouteResolver.normalizeRoute(item.route)
            item.copy(label = resolver(route, item.key, item.label), route = route.ifBlank { item.route })
        }

    return shell.copy(
        bottomPrimary = localize(shell.bottomPrimary),
        accountQuick = localize(shell.accountQuick),
        moreDrawer = localize(shell.moreDrawer),
        vendorContext = localize(shell.vendorContext),
    )
}

private fun routeTitle(route: String, retailKind: String): String = when {
    route == "dashboard" -> "Dashboard"
    route == "cart" -> "Cart"
    route == "catalog" -> "Catalog"
    route == "vendor" -> "Profile"
    route == "more" -> "More"
    route == "attachment" || route == "vendor/attachment" -> "Attachment"
    route == "vendor/page" -> "Profile"
    route == "vendor/summary" -> "Summary"
    route == "vendor/statement" -> "Statement"
    route == "vendor/payout" -> "Payout"
    route == "vendor/transaction" -> "Transactions"
    route == "message" -> "Messages"
    route == "notification" -> "Notifications"
    route == "vendor/retail/new" -> "New ${retailKindLabel(retailKind)}"
    route == "vendor/order/new" -> "New Order"
    route == "vendor/project/new" -> "New Project"
    route == "vendor/retail" -> "Services"
    route == "vendor/order" -> "Orders"
    route == "vendor/project" -> "Tasks"
    route.startsWith("vendor/retail/") -> "Product"
    route.startsWith("vendor/order/") -> "Order"
    route.startsWith("vendor/project/") -> "Project"
    else -> "1tasker"
}

private fun retailKindLabel(kind: String): String = when (kind) {
    "task" -> "Task"
    "service" -> "Service"
    "goods" -> "Product"
    "project" -> "Project"
    else -> "Listing"
}

private fun retailKindDescription(kind: RetailKind): String = when (kind) {
    RetailKind.Task -> "Post work you need someone to complete."
    RetailKind.Service -> "Offer your skills or professional service."
    RetailKind.Goods -> "Sell a physical or digital item."
    RetailKind.Project -> "Publish a project to the marketplace."
}

private fun retailKindIcon(kind: RetailKind): ImageVector = when (kind) {
    RetailKind.Task -> Icons.Default.ReceiptLong
    RetailKind.Service -> Icons.Default.Storefront
    RetailKind.Goods -> Icons.Default.Inventory2
    RetailKind.Project -> Icons.Default.Dashboard
}

private fun isHandledRoute(route: String?): Boolean = MobileRouteResolver.isCurrentlyRenderable(route)



