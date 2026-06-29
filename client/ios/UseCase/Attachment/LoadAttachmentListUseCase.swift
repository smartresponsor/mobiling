import Foundation

public struct LoadAttachmentListUseCase {
    private let reader: AttachmentReader

    public init(reader: AttachmentReader) {
        self.reader = reader
    }

    public func callAsFunction(ownerType: String, ownerId: String, context: String? = nil, slot: String? = nil) async throws -> AttachmentListPayload {
        try await reader.listAttachment(ownerType: ownerType, ownerId: ownerId, context: context, slot: slot)
    }
}
