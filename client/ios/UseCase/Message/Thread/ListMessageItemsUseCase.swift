import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListMessageItemsUseCase: Sendable {
    private let gateway: MessageThreadGateway

    public init(gateway: MessageThreadGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(threadId: String) async throws -> [MessageItemPayload] {
        try await gateway.listItems(threadId: threadId)
    }
}
