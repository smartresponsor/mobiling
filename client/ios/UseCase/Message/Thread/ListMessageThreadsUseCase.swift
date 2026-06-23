import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListMessageThreadsUseCase: Sendable {
    private let gateway: MessageThreadGateway

    public init(gateway: MessageThreadGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws -> [MessageThreadSummary] {
        try await gateway.listThreads()
    }
}
