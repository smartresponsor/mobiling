import Foundation

public struct Config {
  private let refreshRemoteConfigUseCase: RefreshRemoteConfigUseCase
  private let remoteConfigStore: RemoteConfigStore

  public init(
    refreshRemoteConfigUseCase: RefreshRemoteConfigUseCase = RefreshRemoteConfigUseCase(remoteConfigStore: RemoteConfigStore()),
    remoteConfigStore: RemoteConfigStore = RemoteConfigStore()
  ) {
    self.refreshRemoteConfigUseCase = refreshRemoteConfigUseCase
    self.remoteConfigStore = remoteConfigStore
  }

  public func refresh(baseUrl: String = "https://httpbin.org") async throws -> Bool {
    try await refreshRemoteConfigUseCase(baseUrl: baseUrl)
  }

  public func isFresh(ttlSec: Int = 1800) -> Bool {
    remoteConfigStore.isFresh(ttlSec: ttlSec)
  }

  public func defaultPayload() -> RemoteConfigPayload {
    remoteConfigStore.defaultPayload()
  }
}
