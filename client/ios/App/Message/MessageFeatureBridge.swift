import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for the first business-domain controlled rewire.
public struct MessageFeatureBridge: Sendable {
    private let gateway: MessageThreadGateway

    public init(gateway: MessageThreadGateway) {
        self.gateway = gateway
    }

    public func listThreads() async throws -> [MessageThreadSummary] {
        try await ListMessageThreadsUseCase(gateway: gateway)()
    }

    public func listItems(threadId: String) async throws -> [MessageItemPayload] {
        try await ListMessageItemsUseCase(gateway: gateway)(threadId: threadId)
    }

    public func send(request: SendMessageRequest) async throws -> MessageItemPayload {
        try await SendMessageUseCase(gateway: gateway)(request: request)
    }
}
