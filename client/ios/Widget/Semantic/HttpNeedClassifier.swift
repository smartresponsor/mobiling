import Foundation

struct HttpNeedClassifier: NeedClassifier {
    private let baseUrl: String
    private let session: URLSession

    init(baseUrl: String, session: URLSession) {
        self.baseUrl = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.session = session
    }

    func classify(transcript: String, locale: Locale?) async throws -> RecognizedNeed {
        let boundedTranscript = String(transcript.trimmingCharacters(in: .whitespacesAndNewlines).prefix(1200))
        guard !boundedTranscript.isEmpty else { throw NeedClassifierError.emptyTranscript }
        guard let url = URL(string: baseUrl + "/need/classify") else { throw NeedClassifierError.invalidBaseUrl }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(ClassifyNeedRequest(
            transcript: boundedTranscript,
            locale: locale?.identifier
        ))
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw NeedClassifierError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let payload = try? JSONDecoder().decode(ErrorPayload.self, from: data)
            throw NeedClassifierError.requestFailed(payload?.message ?? "Need classification failed with HTTP \(http.statusCode).")
        }
        let payload = try JSONDecoder().decode(ClassifyNeedResponse.self, from: data)
        return RecognizedNeed(
            isNeed: payload.isNeed,
            category: payload.category,
            summary: payload.summary,
            confidence: min(1, max(0, payload.confidence)),
            classifierVersion: payload.classifierVersion
        )
    }
}

private struct ClassifyNeedRequest: Encodable {
    let transcript: String
    let locale: String?
}

private struct ClassifyNeedResponse: Decodable {
    let isNeed: Bool
    let category: String?
    let summary: String?
    let confidence: Double
    let classifierVersion: String
}

private struct ErrorPayload: Decodable {
    let message: String?
}

private enum NeedClassifierError: LocalizedError {
    case emptyTranscript
    case invalidBaseUrl
    case invalidResponse
    case requestFailed(String)

    var errorDescription: String? {
        switch self {
        case .emptyTranscript: return "Transcript is required."
        case .invalidBaseUrl: return "Need classifier URL is invalid."
        case .invalidResponse: return "Need classifier returned an invalid response."
        case .requestFailed(let message): return message
        }
    }
}
