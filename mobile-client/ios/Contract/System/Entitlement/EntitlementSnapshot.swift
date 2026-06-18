import Foundation

public struct EntitlementSnapshot: Sendable, Equatable {
    public let subjectId: String
    public let grantedCodes: [String]
    public let refreshedAtIso8601: String?

    public init(subjectId: String, grantedCodes: [String] = [], refreshedAtIso8601: String? = nil) {
        self.subjectId = subjectId
        self.grantedCodes = grantedCodes
        self.refreshedAtIso8601 = refreshedAtIso8601
    }
}
