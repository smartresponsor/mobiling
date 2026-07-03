package app.mobiling.client.navigation

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
object MobileLinkParser {
    private const val CUSTOM_PREFIX = "smartresponsor://mobile/"
    private const val UNIVERSAL_PREFIX = "https://app.smartresponsor.com/mobile/"

    fun parse(raw: String?): MobileLink? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) {
            return null
        }

        val normalized = when {
            value.startsWith(CUSTOM_PREFIX) -> value.removePrefix(CUSTOM_PREFIX)
            value.startsWith(UNIVERSAL_PREFIX) -> value.removePrefix(UNIVERSAL_PREFIX)
            value.startsWith("/") -> value.trimStart('/')
            else -> value
        }

        val routeAndQuery = normalized.split("?", limit = 2)
        val route = normalizeRoute(routeAndQuery.firstOrNull().orEmpty())
        if (route.isEmpty()) {
            return null
        }

        return MobileLink(
            raw = value,
            route = route,
            query = routeAndQuery.getOrNull(1)?.let(::parseQuery).orEmpty(),
        )
    }

    private fun normalizeRoute(value: String): String = value
        .trim()
        .trim('/')
        .replace(Regex("/{2,}"), "/")

    private fun parseQuery(value: String): Map<String, String> = value
        .split('&')
        .filter { it.isNotBlank() }
        .mapNotNull { part ->
            val pair = part.split("=", limit = 2)
            val key = pair[0].trim()
            if (key.isBlank()) {
                null
            } else {
                key to pair.getOrNull(1).orEmpty().trim()
            }
        }
        .toMap()
}
