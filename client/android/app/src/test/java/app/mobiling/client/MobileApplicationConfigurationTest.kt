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
    fun semanticTextFallsBackToBackendLabel() {
        val resolver = MobileTextResolver(mapOf("navigation.catalog" to "Catalog"))
        assertEquals("Catalog", resolver.resolve("navigation.catalog", "Backend catalog"))
        assertEquals("Backend vendor", resolver.resolve("navigation.vendor", "Backend vendor"))
        assertEquals("Backend label", resolver.resolve(null, "Backend label"))
    }
}
