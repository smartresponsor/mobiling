import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct SendMessageUseCase: Sendable {
    private let gateway: MessageThreadGateway

    public init(gateway: MessageThreadGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(request: SendMessageRequest) async throws -> MessageItemPayload {
        try await gateway.sendMessage(request: request)
    }
}
