package app.mobiling.client

data class ProductProfile(val code: String)
data class BrandProfile(val code: String)
data class EnvironmentProfile(val code: String, val mobileEdgeBaseUrl: String)
data class InitialDestinationPolicy(val destination: String) {
    fun resolvedRoute(isRenderable: (String) -> Boolean): String {
        val normalized = destination.trim().trim('/').replace(Regex("/{2,}"), "/")
        return normalized.takeIf { it.isNotBlank() && isRenderable(it) } ?: "dashboard"
    }
}

data class CatalogPolicy(val primaryCatalog: String, val enabledCatalogs: Set<String>) {
    init { require(primaryCatalog in enabledCatalogs) { "Primary catalog must be enabled." } }

    fun isCatalogEnabled(catalog: String): Boolean = catalog in enabledCatalogs
    fun isPrimaryCatalogEnabled(): Boolean = isCatalogEnabled(primaryCatalog)
}

enum class MobileTextKey(val semanticKey: String) {
    Dashboard("navigation.dashboard"),
    Catalog("navigation.catalog"),
    Message("navigation.message"),
    Vendor("navigation.vendor"),
}

class MobileTextResolver(private val localText: Map<String, String>) {
    fun resolve(semanticKey: String?, backendLabel: String): String =
        semanticKey?.let(localText::get)?.takeIf(String::isNotBlank) ?: backendLabel

    fun resolveNavigation(route: String?, key: String, backendLabel: String): String =
        resolve(navigationSemanticKey(route, key), backendLabel)

    private fun navigationSemanticKey(route: String?, key: String): String? {
        val root = route?.trim('/')?.substringBefore('/')?.takeIf(String::isNotBlank) ?: key.substringBefore('_')
        return MobileTextKey.entries.firstOrNull { it.name.equals(root, ignoreCase = true) }?.semanticKey
    }
}

data class MobileApplicationConfiguration(
    val product: ProductProfile,
    val brand: BrandProfile,
    val environment: EnvironmentProfile,
    val initialDestination: InitialDestinationPolicy,
    val catalog: CatalogPolicy,
    val textResolver: MobileTextResolver,
)

object MobileApplicationConfigurationFactory {
    fun current(localText: Map<String, String> = emptyMap()): MobileApplicationConfiguration = MobileApplicationConfiguration(
        product = ProductProfile(BuildConfig.PRODUCT_PROFILE),
        brand = BrandProfile(BuildConfig.BRAND_PROFILE),
        environment = EnvironmentProfile(BuildConfig.ENVIRONMENT_PROFILE, BuildConfig.MOBILE_EDGE_BASE_URL),
        initialDestination = InitialDestinationPolicy(BuildConfig.INITIAL_DESTINATION),
        catalog = CatalogPolicy(
            primaryCatalog = BuildConfig.PRIMARY_CATALOG,
            enabledCatalogs = BuildConfig.ENABLED_CATALOGS.split(',').map(String::trim).filter(String::isNotBlank).toSet(),
        ),
        textResolver = MobileTextResolver(localText),
    )
}
