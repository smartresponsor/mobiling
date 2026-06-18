import MobileClient
import Foundation

public struct Analytic {
  private let recordAnalyticEventUseCase: RecordAnalyticEventUseCase
  private let analyticEventRecorder: AnalyticEventRecorder

  public init(
    analyticEventRecorder: AnalyticEventRecorder = AnalyticEventRecorder()
  ) {
    self.analyticEventRecorder = analyticEventRecorder
    self.recordAnalyticEventUseCase = RecordAnalyticEventUseCase(recorder: analyticEventRecorder)
  }

  @discardableResult
  public func log(event: String, prop: [String: Any] = [:]) -> AnalyticEventPayload {
    recordAnalyticEventUseCase(analyticPayload(event: event, prop: prop))
  }

  public func record(_ payload: AnalyticEventPayload) -> AnalyticEventPayload {
    recordAnalyticEventUseCase(payload)
  }

  public func analyticPayload(event: String, prop: [String: Any] = [:]) -> AnalyticEventPayload {
    AnalyticEventPayload(
      name: event,
      attributes: stringifyAttributes(prop),
      occurredAtIso8601: ISO8601DateFormatter().string(from: Date())
    )
  }

  private func stringifyAttributes(_ prop: [String: Any]) -> [String: String] {
    prop.reduce(into: [String: String]()) { partialResult, entry in
      partialResult[entry.key] = String(describing: entry.value)
    }
  }
}
