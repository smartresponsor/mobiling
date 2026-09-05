package app.mobiling.client

object MobileClientRuntimeConfig {
    val configuration: MobileApplicationConfiguration
        get() = MobileApplicationConfigurationFactory.current()

    val mobileEdgeBaseUrl: String
        get() = configuration.environment.mobileEdgeBaseUrl
}
