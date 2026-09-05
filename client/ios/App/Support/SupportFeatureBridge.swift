import Foundation

public struct SupportFeatureBridge: Sendable {
    private let gateway: SupportGateway

    public init(gateway: SupportGateway) {
        self.gateway = gateway
    }

    public func load(path: String) async throws -> SupportPagePayload {
        try await gateway.load(path: path)
    }

    public func submit(path: String, fields: [String: String]) async throws -> SupportPagePayload {
        try await gateway.submit(path: path, fields: fields)
    }
}
