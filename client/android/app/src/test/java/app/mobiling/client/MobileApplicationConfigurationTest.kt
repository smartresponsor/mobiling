package app.mobiling.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileApplicationConfigurationTest {
    @Test
    fun primaryCatalogMustRemainEnabled() {
        val policy = CatalogPolicy("service", setOf("service", "product", "project"))
        assertTrue(policy.primaryCatalog in policy.enabledCatalogs)
    }

    @Test
    fun initialDestinationNormalizesAndFallsBack() {
        assertEquals("vendor", InitialDestinationPolicy("//vendor//").resolvedRoute { it == "vendor" })
        assertEquals("dashboard", InitialDestinationPolicy("unknown").resolvedRoute { false })
    }

    @Test
    fun publicInitialDestinationNormalizesAndFallsBack() {
        assertEquals("catalog", PublicInitialDestinationPolicy("//CATALOG//").resolvedRoute { it == "catalog" })
        assertEquals("home", PublicInitialDestinationPolicy("unknown").resolvedRoute { false })
    }

    @Test
    fun catalogPolicyExposesPrimaryAvailability() {
        val policy = CatalogPolicy("service", setOf("service", "product"))
        assertTrue(policy.isPrimaryCatalogEnabled())
        assertTrue(policy.isCatalogEnabled("product"))
    }

    @Test
    fun navigationTextResolvesKnownRootsAndPreservesUnknownLabels() {
        val resolver = MobileTextResolver(
            mapOf(
                MobileTextKey.Dashboard.semanticKey to "Local dashboard",
                MobileTextKey.Tasks.semanticKey to "Local tasks",
                MobileTextKey.Services.semanticKey to "Local services",
                MobileTextKey.Profile.semanticKey to "Local profile",
            ),
        )
        assertEquals("Local dashboard", resolver.resolveNavigation("dashboard", "dashboard", "Backend dashboard"))
        assertEquals("Local tasks", resolver.resolveNavigation("vendor/project", "tasks", "Backend vendor"))
        assertEquals("Local services", resolver.resolveNavigation("vendor/retail", "services", "Backend vendor"))
        assertEquals("Local profile", resolver.resolveNavigation("vendor/page", "vendor_page", "Backend vendor"))
        assertEquals("Backend attachment", resolver.resolveNavigation("attachment", "attachment", "Backend attachment"))
    }

    @Test
    fun semanticTextFallsBackToBackendLabel() {
        val resolver = MobileTextResolver(mapOf("navigation.catalog" to "Catalog"))
        assertEquals("Catalog", resolver.resolve("navigation.catalog", "Backend catalog"))
        assertEquals("Backend vendor", resolver.resolve("navigation.vendor", "Backend vendor"))
        assertEquals("Backend label", resolver.resolve(null, "Backend label"))
    }
}
