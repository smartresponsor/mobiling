package app.mobiling.client

data class MobileApplicationComposition(
    val configuration: MobileApplicationConfiguration,
) {
    val mobileEdgeBaseUrl: String
        get() = configuration.environment.mobileEdgeBaseUrl
}

object MobileApplicationComposer {
    fun current(localText: Map<String, String> = emptyMap()): MobileApplicationComposition =
        MobileApplicationComposition(MobileApplicationConfigurationFactory.current(localText))
}
