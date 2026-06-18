import Foundation

public struct EntitlementSnapshotGateway {
    public init() {}

    public func fetch(subjectId: String) -> EntitlementSnapshot {
        EntitlementSnapshot(subjectId: subjectId)
    }
}
