import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct MessageThreadIndexState: Sendable {
    public let threads: [MessageThreadSummary]

    public init(threads: [MessageThreadSummary] = []) {
        self.threads = threads
    }
}

public struct MessageThreadDetailState: Sendable {
    public let threadId: String
    public let items: [MessageItemPayload]

    public init(threadId: String, items: [MessageItemPayload] = []) {
        self.threadId = threadId
        self.items = items
    }
}
