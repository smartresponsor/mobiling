package app.mobiling.client.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.contract.navigation.shell.MobileNavigationItemPayload
import app.mobiling.client.data.navigation.shell.NavigationShellGateway
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import app.mobiling.client.data.vendor.statement.VendorStatementGateway
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import app.mobiling.client.ui.navigation.shell.MobileNavigationShellScreenContract
import app.mobiling.client.usecase.navigation.shell.LoadNavigationShellUseCase
import app.mobiling.client.vendor.MobileVendorPayoutScreen
import app.mobiling.client.vendor.MobileVendorProfileScreen
import app.mobiling.client.vendor.MobileVendorStatementScreen
import app.mobiling.client.vendor.MobileVendorSummaryScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDashboardShell(
    navigationShellGateway: NavigationShellGateway?,
    vendorId: String? = null,
    vendorProfileGateway: VendorProfileGateway? = null,
    vendorSummaryGateway: VendorSummaryGateway? = null,
    vendorStatementGateway: VendorStatementGateway? = null,
    vendorPayoutGateway: VendorPayoutGateway? = null,
    onSignOut: () -> Unit,
) {
    var selectedRoute by remember { mutableStateOf("dashboard") }
    var accountOpen by remember { mutableStateOf(false) }
    var shell by remember { mutableStateOf(fallbackShell()) }

    LaunchedEffect(navigationShellGateway) {
        if (navigationShellGateway != null) {
            shell = try {
                MobileNavigationShellScreenContract.from(LoadNavigationShellUseCase(navigationShellGateway).invoke())
            } catch (_: Exception) {
                fallbackShell()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SmartResponsor", fontWeight = FontWeight.SemiBold)
                        Text("Mobile dashboard")
                    }
                },
                actions = {
                    TextButton(onClick = { accountOpen = true }) {
                        Text("Account")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                shell.bottomPrimary.filter { it.visible }.forEach { item ->
                    NavigationBarItem(
                        selected = selectedRoute == item.route,
                        onClick = {
                            if (item.enabled && isHandledRoute(item.route)) {
                                selectedRoute = item.route ?: item.key
                            }
                        },
                        icon = { Text(iconLabel(item)) },
                        label = { Text(item.label) },
                        enabled = item.enabled,
                    )
                }
            }
        },
    ) { padding ->
        DashboardContent(
            selectedRoute = selectedRoute,
            shell = shell,
            vendorId = vendorId,
            vendorProfileGateway = vendorProfileGateway,
            vendorSummaryGateway = vendorSummaryGateway,
            vendorStatementGateway = vendorStatementGateway,
            vendorPayoutGateway = vendorPayoutGateway,
            onRouteSelected = { route -> if (isHandledRoute(route)) selectedRoute = route },
            padding = padding,
        )
    }

    if (accountOpen) {
        ModalBottomSheet(onDismissRequest = { accountOpen = false }) {
            ShellSection(
                title = "Account",
                items = shell.accountQuick,
                onItemClick = { item ->
                    when {
                        item.action == "access.sign_out" -> onSignOut()
                        item.enabled && isHandledRoute(item.route) -> selectedRoute = item.route ?: item.key
                        else -> Unit
                    }
                    accountOpen = false
                },
            )
        }
    }
}

@Composable
private fun DashboardContent(
    selectedRoute: String,
    shell: MobileNavigationShellScreenContract,
    vendorId: String?,
    vendorProfileGateway: VendorProfileGateway?,
    vendorSummaryGateway: VendorSummaryGateway?,
    vendorStatementGateway: VendorStatementGateway?,
    vendorPayoutGateway: VendorPayoutGateway?,
    onRouteSelected: (String) -> Unit,
    padding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = when (selectedRoute) {
                    "vendor" -> "Vendor"
                    "vendor/profile" -> "My Profile"
                    "vendor/summary" -> "Vendor Summary"
                    "vendor/statement" -> "Vendor Statement"
                    "vendor/payout" -> "Vendor Payout"
                    "vendor/transaction" -> "Vendor Transaction"
                    "more" -> "More"
                    else -> "Dashboard"
                },
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Text("Root shell is loaded from Navigating publication. Inactive modules stay visible as Coming soon.")
        }

        when (selectedRoute) {
            "vendor" -> item {
                ShellSection(title = "Vendor", items = shell.vendorContext, onItemClick = { item ->
                    item.route?.let(onRouteSelected)
                })
            }
            "vendor/profile" -> item {
                MobileVendorProfileScreen(vendorId = vendorId, vendorProfileGateway = vendorProfileGateway)
            }
            "vendor/summary" -> item {
                MobileVendorSummaryScreen(vendorId = vendorId, vendorSummaryGateway = vendorSummaryGateway)
            }
            "vendor/statement" -> item {
                MobileVendorStatementScreen(vendorId = vendorId, vendorStatementGateway = vendorStatementGateway)
            }
            "vendor/payout" -> item {
                MobileVendorPayoutScreen(vendorId = vendorId, vendorPayoutGateway = vendorPayoutGateway)
            }
            "vendor/transaction" -> item {
                Text("Vendor Transaction will be connected to mobile-edge vendor data.")
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
private fun ShellSection(
    title: String,
    items: List<MobileNavigationItemPayload>,
    onItemClick: (MobileNavigationItemPayload) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
        items.filter { it.visible }.forEach { item ->
            ListItem(
                headlineContent = { Text(item.label) },
                supportingContent = { Text(item.badge ?: item.route ?: item.key) },
                leadingContent = { Text(iconLabel(item)) },
                trailingContent = {
                    if (!item.enabled) {
                        AssistChip(
                            onClick = {},
                            label = { Text(item.badge ?: "Coming soon") },
                        )
                    }
                },
                modifier = Modifier.clickable(enabled = item.enabled) {
                    onItemClick(item)
                },
            )
        }
    }
}

private fun iconLabel(item: MobileNavigationItemPayload): String =
    when (item.icon) {
        "store" -> "🏬"
        "person" -> "👤"
        "attachment" -> "📎"
        "message" -> "💬"
        "catalog" -> "🛍"
        "key" -> "🔑"
        "logout" -> "↩"
        "summary" -> "📊"
        "statement" -> "🧾"
        "payout" -> "💵"
        "receipt" -> "🧾"
        "menu" -> "☰"
        else -> "⌂"
    }

private fun fallbackShell(): MobileNavigationShellScreenContract = MobileNavigationShellScreenContract(
    bottomPrimary = listOf(
        item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
        item("vendor", "Vendor", "store", true, "vendor"),
        item("more", "More", "menu", true, "more"),
    ),
    accountQuick = listOf(
        item("vendor_profile", "My Profile", "person", true, "vendor/profile"),
        item("access_password", "Change Password", "key", false, "access/password"),
        item("access_verification", "Verification", "key", false, "access/verification"),
        item("vendor_attachment", "My Attachments", "attachment", false, "attachment"),
        item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action = "access.sign_out"),
    ),
    moreDrawer = listOf(
        item("dashboard", "Dashboard", "dashboard", true, "dashboard"),
        item("vendor", "Vendor", "store", true, "vendor"),
        item("catalog", "Catalog", "catalog", false, "catalog"),
        item("message", "Message", "message", false, "message"),
        item("attachment", "Attachments", "attachment", false, "attachment"),
    ),
    vendorContext = listOf(
        item("vendor_overview", "My Vendor", "store", true, "vendor"),
        item("vendor_profile", "My Profile", "person", true, "vendor/profile"),
        item("vendor_summary", "Summary", "summary", true, "vendor/summary"),
        item("vendor_statement", "Statement", "statement", true, "vendor/statement"),
        item("vendor_payout", "Payout", "payout", true, "vendor/payout"),
        item("vendor_transaction", "Transaction", "receipt", true, "vendor/transaction"),
        item("vendor_attachment", "My Attachments", "attachment", false, "attachment"),
    ),
)

private fun item(
    key: String,
    label: String,
    icon: String,
    enabled: Boolean,
    route: String,
    action: String? = null,
): MobileNavigationItemPayload = MobileNavigationItemPayload(
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

private fun isHandledRoute(route: String?): Boolean =
    setOf("dashboard", "vendor", "vendor/profile", "vendor/summary", "vendor/statement", "vendor/payout", "vendor/transaction", "more").contains(route)


