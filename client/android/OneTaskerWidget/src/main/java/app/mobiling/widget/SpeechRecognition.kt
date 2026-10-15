package app.mobiling.widget

data class SpeechRecognitionResult(
    val transcript: String,
    val isFinal: Boolean = true,
    val usedOnDevice: Boolean,
)

interface SpeechRecognition {
    suspend fun recognizeOnce(localeTag: String? = null): SpeechRecognitionResult
}
