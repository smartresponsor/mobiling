import Foundation

public struct RefreshRemoteConfigUseCase {
    private let remoteConfigStore: RemoteConfigStore

    public init(remoteConfigStore: RemoteConfigStore) {
        self.remoteConfigStore = remoteConfigStore
    }

    public func callAsFunction(baseUrl: String = "https://httpbin.org") async throws -> Bool {
        try await remoteConfigStore.refresh(baseUrl: baseUrl)
    }
}
