import Foundation

public struct RecordAnalyticEventUseCase {
    private let recorder: AnalyticEventRecorder

    public init(recorder: AnalyticEventRecorder = AnalyticEventRecorder()) {
        self.recorder = recorder
    }

    public func callAsFunction(_ payload: AnalyticEventPayload) -> AnalyticEventPayload {
        recorder.record(payload)
    }
}
