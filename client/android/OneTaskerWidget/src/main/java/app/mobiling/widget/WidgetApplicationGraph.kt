package app.mobiling.widget

import android.content.Context
import app.mobiling.client.data.auth.session.AccessHttpAuthSessionGateway
import app.mobiling.client.data.notification.NotificationHttpGateway
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class WidgetApplicationGraph private constructor(
    val authGateway: AccessHttpAuthSessionGateway,
    val notificationGateway: NotificationHttpGateway,
    val speechRecognition: SpeechRecognition,
    val needClassifier: NeedClassifier,
) {
    companion object {
        fun current(context: Context): WidgetApplicationGraph {
            val cookies = mutableListOf<Cookie>()
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("X-Application-Key", BuildConfig.PRODUCT_PROFILE)
                        .header("X-Application-Environment", BuildConfig.ENVIRONMENT_PROFILE)
                        .build()
                    chain.proceed(request)
                }
                .cookieJar(object : CookieJar {
                    @Synchronized
                    override fun saveFromResponse(url: HttpUrl, incoming: List<Cookie>) {
                        val now = System.currentTimeMillis()
                        cookies.removeAll { existing ->
                            existing.expiresAt <= now || incoming.any { candidate ->
                                existing.name == candidate.name && existing.domain == candidate.domain && existing.path == candidate.path
                            }
                        }
                        cookies += incoming.filter { it.expiresAt > now }
                    }

                    @Synchronized
                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        val now = System.currentTimeMillis()
                        cookies.removeAll { it.expiresAt <= now }
                        return cookies.filter { it.matches(url) }
                    }
                })
                .build()

            val baseUrl = BuildConfig.MOBILE_EDGE_BASE_URL
            return WidgetApplicationGraph(
                authGateway = AccessHttpAuthSessionGateway(baseUrl, client),
                notificationGateway = NotificationHttpGateway(baseUrl, client),
                speechRecognition = AndroidNativeSpeechRecognition(context.applicationContext),
                needClassifier = HttpNeedClassifier(baseUrl, client),
            )
        }
    }
}
