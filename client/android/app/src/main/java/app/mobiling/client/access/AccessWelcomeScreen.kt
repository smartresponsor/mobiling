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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.cart.CartMobileScreen
import app.mobiling.client.catalog.CatalogFeatureBridge
import app.mobiling.client.catalog.CatalogMobileScreen
import app.mobiling.client.navigation.CanonicalBottomNavigation
import app.mobiling.client.navigation.CanonicalBottomNavigationItem
import app.mobiling.client.navigation.CanonicalTopAppBar
import app.mobiling.client.design.MobileDesignSystem

private enum class GuestRoute(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Default.Home),
    Catalog("catalog", "Catalog", Icons.Default.Inventory2),
    User("users", "Users", Icons.Default.People),
    Order("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    Cart("cart", "Cart", Icons.Default.ShoppingCart);

    companion object {
        fun fromRoute(route: String): GuestRoute = entries.firstOrNull { it.route == route } ?: Home
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessWelcomeScreen(
    initialRoute: String = "home",
    catalogFeatureBridge: CatalogFeatureBridge? = null,
    cartFeatureBridge: CartFeatureBridge? = null,
    onSignIn: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    var selectedRoute by remember(initialRoute) { mutableStateOf(GuestRoute.fromRoute(initialRoute)) }
    var navigationOpen by remember { mutableStateOf(false) }
    var accountOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CanonicalTopAppBar(
                title = selectedRoute.label,
                navigationIcon = {
                    IconButton(onClick = { navigationOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation")
                    }
                },
                actions = {
                    IconButton(onClick = { accountOpen = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Guest account")
                    }
                },
            )
        },
        bottomBar = {
            CanonicalBottomNavigation(
                items = GuestRoute.entries.map { route ->
                    CanonicalBottomNavigationItem(
                        key = route.route,
                        label = route.label,
                        icon = route.icon,
                        selected = selectedRoute == route,
                        onClick = {
                            selectedRoute = route
                        },
                    )
                },
            )
        },
    ) { padding ->
        GuestRouteContent(
            route = selectedRoute,
            padding = padding,
            catalogFeatureBridge = catalogFeatureBridge,
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
                    .padding(horizontal = MobileDesignSystem.spacing.xl, vertical = MobileDesignSystem.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
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
                            modifier = Modifier.fillMaxWidth().padding(MobileDesignSystem.spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.lg),
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
                Spacer(Modifier.height(MobileDesignSystem.spacing.xxl))
            }
        }
    }

    if (accountOpen) {
        ModalBottomSheet(onDismissRequest = { accountOpen = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = MobileDesignSystem.spacing.xxl, vertical = MobileDesignSystem.spacing.md),
                verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.md),
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
    cartFeatureBridge: CartFeatureBridge?,
    onSignIn: () -> Unit,
    onCreateAccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(MobileDesignSystem.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MobileDesignSystem.spacing.lg),
    ) {
        when (route) {
            GuestRoute.Home -> {
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
                OutlinedButton(onClick = onCreateAccess, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
                GuestLegalFooter()
            }
            GuestRoute.Catalog -> CatalogMobileScreen(catalogFeatureBridge = catalogFeatureBridge)
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
        Text(description, modifier = Modifier.padding(MobileDesignSystem.spacing.lg))
    }
}
