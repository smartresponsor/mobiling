import Foundation
import WidgetClient

struct WidgetApplicationGraph {
    let authGateway: HttpAuthSessionGateway
    let notificationGateway: NotificationHttpGateway
    let speechRecognition: SpeechRecognition
    let needClassifier: NeedClassifier

    static func current(bundle: Bundle = .main) -> WidgetApplicationGraph {
        let baseUrl = bundle.object(forInfoDictionaryKey: "MOBILE_EDGE_BASE_URL") as? String ?? "http://localhost:8080"
        let product = bundle.object(forInfoDictionaryKey: "MOBILE_PRODUCT_PROFILE") as? String ?? "one_tasker_widget"
        let environment = bundle.object(forInfoDictionaryKey: "MOBILE_ENVIRONMENT_PROFILE") as? String ?? "local"

        let configuration = URLSessionConfiguration.default
        configuration.httpCookieStorage = .shared
        configuration.httpShouldSetCookies = true
        configuration.httpAdditionalHeaders = [
            "X-Application-Key": product,
            "X-Application-Environment": environment,
        ]
        let session = URLSession(configuration: configuration)

        return WidgetApplicationGraph(
            authGateway: HttpAuthSessionGateway(baseUrl: baseUrl, session: session),
            notificationGateway: NotificationHttpGateway(baseUrl: baseUrl, session: session),
            speechRecognition: IOSNativeSpeechRecognition(),
            needClassifier: HttpNeedClassifier(baseUrl: baseUrl, session: session)
        )
    }
}
