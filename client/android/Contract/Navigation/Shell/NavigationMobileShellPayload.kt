package app.mobiling.client.contract.navigation.shell

data class NavigationMobileShellPayload(
    val schema: String,
    val channel: String,
    val platforms: List<String>,
    val locations: Map<String, List<NavigationMobileItemPayload>>,
) {
    fun items(location: String): List<NavigationMobileItemPayload> = locations[location].orEmpty()
}

data class NavigationMobileItemPayload(
    val key: String,
    val label: String,
    val icon: String?,
    val badge: String?,
    val enabled: Boolean,
    val visible: Boolean,
    val status: String,
    val disabledReason: String?,
    val requiredComponent: String?,
    val location: String,
    val group: String,
    val groupLabel: String,
    val action: String?,
    val route: String?,
)
