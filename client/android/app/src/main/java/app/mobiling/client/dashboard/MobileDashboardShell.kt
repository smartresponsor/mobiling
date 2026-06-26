package app.mobiling.client.dashboard

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
import app.mobiling.client.ui.navigation.shell.MobileNavigationShellScreenContract
import app.mobiling.client.usecase.navigation.shell.LoadNavigationShellUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDashboardShell(
    navigationShellGateway: NavigationShellGateway?,
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
                            if (item.enabled) {
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
                        item.enabled -> selectedRoute = item.route ?: item.key
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
                ShellSection(title = "Vendor", items = shell.vendorContext)
            }
            "more" -> item {
                ShellSection(title = "More", items = shell.moreDrawer)
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
