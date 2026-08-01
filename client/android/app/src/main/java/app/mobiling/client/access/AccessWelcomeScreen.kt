package app.mobiling.client.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.cart.CartMobileScreen
import app.mobiling.client.catalog.CatalogFeatureBridge
import app.mobiling.client.catalog.CatalogMobileScreen

private enum class GuestRoute(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Catalog("Catalog", Icons.Default.Inventory2),
    User("Users", Icons.Default.People),
    Order("Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    Cart("Cart", Icons.Default.ShoppingCart),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessWelcomeScreen(
    catalogFeatureBridge: CatalogFeatureBridge? = null,
    cartFeatureBridge: CartFeatureBridge? = null,
    onSignIn: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    var selectedRoute by remember { mutableStateOf(GuestRoute.Home) }
    var catalogRootRequest by remember { mutableIntStateOf(0) }
    var navigationOpen by remember { mutableStateOf(false) }
    var accountOpen by remember { mutableStateOf(false) }

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
                    Text(selectedRoute.label, fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = { accountOpen = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Guest account")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                GuestRoute.entries.forEach { route ->
                    NavigationBarItem(
                        selected = selectedRoute == route,
                        onClick = {
                            if (route == GuestRoute.Catalog && selectedRoute == GuestRoute.Catalog) {
                                catalogRootRequest += 1
                            }
                            selectedRoute = route
                        },
                        icon = { Icon(route.icon, contentDescription = route.label) },
                        label = { Text(route.label) },
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
    ) { padding ->
        GuestRouteContent(
            route = selectedRoute,
            padding = padding,
            catalogFeatureBridge = catalogFeatureBridge,
            catalogRootRequest = catalogRootRequest,
            cartFeatureBridge = cartFeatureBridge,
            onSignIn = onSignIn,
            onCreateAccess = onCreateAccess,
        )
    }

    if (navigationOpen) {
        ModalBottomSheet(onDismissRequest = { navigationOpen = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GuestRoute.entries.forEach { route ->
                    ElevatedCard(
                        onClick = {
                            selectedRoute = route
                            navigationOpen = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(route.icon, contentDescription = null)
                            Text(
                                text = route.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (accountOpen) {
        ModalBottomSheet(onDismissRequest = { accountOpen = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Guest account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        accountOpen = false
                        onSignIn()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign in")
                }
                OutlinedButton(
                    onClick = {
                        accountOpen = false
                        onCreateAccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create account")
                }
                GuestLegalFooter()
            }
        }
    }
}

@Composable
private fun GuestRouteContent(
    route: GuestRoute,
    padding: PaddingValues,
    catalogFeatureBridge: CatalogFeatureBridge?,
    catalogRootRequest: Int,
    cartFeatureBridge: CartFeatureBridge?,
    onSignIn: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (route) {
            GuestRoute.Home -> {
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
                OutlinedButton(onClick = onCreateAccess, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
                GuestLegalFooter()
            }
            GuestRoute.Catalog -> CatalogMobileScreen(
                catalogFeatureBridge = catalogFeatureBridge,
                rootRequest = catalogRootRequest,
            )
            GuestRoute.Cart -> CartMobileScreen(cartFeatureBridge = cartFeatureBridge)
            GuestRoute.User -> GuestPlaceholder(
                description = "Public customer, specialist, vendor, and sponsor profiles will be available here without exposing private account data.",
            )
            GuestRoute.Order -> GuestPlaceholder(
                description = "Guests can start sponsorship or checkout activity here. Personal order history remains available only after authentication.",
            )
        }
    }
}

@Composable
private fun GuestPlaceholder(description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Text(description, modifier = Modifier.padding(16.dp))
    }
}
