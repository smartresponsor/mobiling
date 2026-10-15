package app.mobiling.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidNativeSpeechRecognition(
    context: Context,
) : SpeechRecognition {
    private val appContext = context.applicationContext

    override suspend fun recognizeOnce(localeTag: String?): SpeechRecognitionResult =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val onDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
                val recognizer = if (onDevice) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(appContext)
                }

                fun finish() {
                    recognizer.cancel()
                    recognizer.destroy()
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle) {
                        if (!continuation.isActive) {
                            finish()
                            return
                        }
                        val transcript = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                            .trim()
                        finish()
                        continuation.resume(
                            SpeechRecognitionResult(
                                transcript = transcript,
                                usedOnDevice = onDevice,
                            ),
                        )
                    }

                    override fun onError(error: Int) {
                        if (!continuation.isActive) {
                            finish()
                            return
                        }
                        finish()
                        continuation.resumeWithException(
                            IllegalStateException("Speech recognition failed with code $error."),
                        )
                    }

                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })

                continuation.invokeOnCancellation { finish() }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    .putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        localeTag?.takeIf(String::isNotBlank) ?: Locale.getDefault().toLanguageTag(),
                    )
                recognizer.startListening(intent)
            }
        }
}
