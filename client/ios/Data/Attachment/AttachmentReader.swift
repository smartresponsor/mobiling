import Foundation

public protocol AttachmentReader {
    func listAttachment(ownerType: String, ownerId: String, context: String?, slot: String?) async throws -> AttachmentListPayload
}
