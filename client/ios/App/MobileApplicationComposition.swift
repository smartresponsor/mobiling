import CoreConfig

struct MobileApplicationComposition {
    let configuration: MobileApplicationConfiguration

    var mobileEdgeBaseUrl: String {
        configuration.environment.mobileEdgeBaseUrl
    }

    static let current = MobileApplicationComposition(
        configuration: MobileClientRuntimeConfig.configuration
    )
}
