package app.mobiling.widget

data class RecognizedNeed(
    val isNeed: Boolean,
    val category: String?,
    val summary: String?,
    val confidence: Double,
    val classifierVersion: String,
)

interface NeedClassifier {
    suspend fun classify(
        transcript: String,
        localeTag: String? = null,
    ): RecognizedNeed
}
