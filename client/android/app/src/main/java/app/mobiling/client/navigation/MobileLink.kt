package app.mobiling.client.navigation

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MobileLink(
    val raw: String,
    val route: String,
    val query: Map<String, String> = emptyMap(),
)
