import Foundation

struct RecognizedNeed {
    let isNeed: Bool
    let category: String?
    let summary: String?
    let confidence: Double
    let classifierVersion: String
}

protocol NeedClassifier {
    func classify(transcript: String, locale: Locale?) async throws -> RecognizedNeed
}
