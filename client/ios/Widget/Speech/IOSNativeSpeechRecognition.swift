import AVFoundation
import Foundation
import Speech

final class IOSNativeSpeechRecognition: NSObject, SpeechRecognition {
    private let audioEngine = AVAudioEngine()
    private var recognitionTask: SFSpeechRecognitionTask?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var tapInstalled = false

    func recognizeOnce(locale: Locale?) async throws -> SpeechRecognitionResult {
        try await authorize()

        return try await withCheckedThrowingContinuation { continuation in
            guard let recognizer = SFSpeechRecognizer(locale: locale ?? .current), recognizer.isAvailable else {
                continuation.resume(throwing: SpeechRecognitionError.unavailable)
                return
            }

            let request = SFSpeechAudioBufferRecognitionRequest()
            request.shouldReportPartialResults = false
            let useOnDevice = recognizer.supportsOnDeviceRecognition
            request.requiresOnDeviceRecognition = useOnDevice
            recognitionRequest = request

            let session = AVAudioSession.sharedInstance()
            do {
                try session.setCategory(.record, mode: .measurement, options: [.duckOthers])
                try session.setActive(true, options: .notifyOthersOnDeactivation)

                let inputNode = audioEngine.inputNode
                let format = inputNode.outputFormat(forBus: 0)
                inputNode.installTap(onBus: 0, bufferSize: 1024, format: format) { buffer, _ in
                    request.append(buffer)
                }
                tapInstalled = true

                audioEngine.prepare()
                try audioEngine.start()
            } catch {
                cleanup()
                continuation.resume(throwing: error)
                return
            }

            var completed = false
            recognitionTask = recognizer.recognitionTask(with: request) { [weak self] result, error in
                guard !completed else { return }

                if let result, result.isFinal {
                    completed = true
                    let transcript = result.bestTranscription.formattedString.trimmingCharacters(in: .whitespacesAndNewlines)
                    self?.cleanup()
                    continuation.resume(
                        returning: SpeechRecognitionResult(
                            transcript: transcript,
                            isFinal: true,
                            usedOnDevice: useOnDevice
                        )
                    )
                    return
                }

                if let error {
                    completed = true
                    self?.cleanup()
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    private func authorize() async throws {
        let speechStatus = await withCheckedContinuation { continuation in
            SFSpeechRecognizer.requestAuthorization { continuation.resume(returning: $0) }
        }
        guard speechStatus == .authorized else {
            throw SpeechRecognitionError.speechPermissionDenied
        }

        let microphoneGranted = await withCheckedContinuation { continuation in
            AVAudioSession.sharedInstance().requestRecordPermission { continuation.resume(returning: $0) }
        }
        guard microphoneGranted else {
            throw SpeechRecognitionError.microphonePermissionDenied
        }
    }

    private func cleanup() {
        if audioEngine.isRunning {
            audioEngine.stop()
        }
        if tapInstalled {
            audioEngine.inputNode.removeTap(onBus: 0)
            tapInstalled = false
        }
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        recognitionTask = nil
        recognitionRequest = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}

enum SpeechRecognitionError: Error {
    case unavailable
    case speechPermissionDenied
    case microphonePermissionDenied
}
