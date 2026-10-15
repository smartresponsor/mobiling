import Foundation

public struct AttachmentItemPayload: Sendable, Identifiable {
    public let id: String
    public let attachmentId: String
    public let type: String?
    public let mediaKind: String?
    public let documentKind: String?
    public let originalName: String?
    public let title: String?
    public let mimeType: String?
    public let extensionName: String?
    public let size: Int64
    public let width: Int?
    public let height: Int?
    public let durationMs: Int?
    public let pageCount: Int?
    public let context: String?
    public let slot: String?
    public let isPrimary: Bool
    public let position: Int
    public let createdAt: String?
    public let downloadUrl: String?
    public let payloadText: String?

    public init(
        attachmentId: String,
        type: String?,
        mediaKind: String? = nil,
        documentKind: String? = nil,
        originalName: String? = nil,
        title: String? = nil,
        mimeType: String? = nil,
        extensionName: String? = nil,
        size: Int64 = 0,
        width: Int? = nil,
        height: Int? = nil,
        durationMs: Int? = nil,
        pageCount: Int? = nil,
        context: String? = nil,
        slot: String? = nil,
        isPrimary: Bool = false,
        position: Int = 0,
        createdAt: String? = nil,
        downloadUrl: String? = nil,
        payloadText: String? = nil
    ) {
        self.id = "\(attachmentId):\(context ?? ""):\(slot ?? ""):\(position)"
        self.attachmentId = attachmentId
        self.type = type
        self.mediaKind = mediaKind
        self.documentKind = documentKind
        self.originalName = originalName
        self.title = title
        self.mimeType = mimeType
        self.extensionName = extensionName
        self.size = size
        self.width = width
        self.height = height
        self.durationMs = durationMs
        self.pageCount = pageCount
        self.context = context
        self.slot = slot
        self.isPrimary = isPrimary
        self.position = position
        self.createdAt = createdAt
        self.downloadUrl = downloadUrl
        self.payloadText = payloadText
    }
}
