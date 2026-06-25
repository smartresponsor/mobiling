import SwiftUI
import MobileClient
struct ContentView: View {
    var body: some View {
        MobilingAppShell(
            authFeatureBridge: AuthFeatureBridge(
                gateway: HttpAuthSessionGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl)
            )
        )
    }
}
