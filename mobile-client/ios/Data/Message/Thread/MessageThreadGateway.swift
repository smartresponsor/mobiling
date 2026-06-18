import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol MessageThreadGateway: Sendable {
    func listThreads() async throws -> [MessageThreadSummary]
    func listItems(threadId: String) async throws -> [MessageItemPayload]
    func sendMessage(request: SendMessageRequest) async throws -> MessageItemPayload
}
