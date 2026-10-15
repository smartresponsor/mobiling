import Foundation

struct SpeechRecognitionResult {
    let transcript: String
    let isFinal: Bool
    let usedOnDevice: Bool
}

protocol SpeechRecognition {
    func recognizeOnce(locale: Locale?) async throws -> SpeechRecognitionResult
}
