import Foundation

public struct RefreshEntitlementSnapshotUseCase {
    private let gateway: EntitlementSnapshotGateway

    public init(gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway()) {
        self.gateway = gateway
    }

    public func callAsFunction(subjectId: String) -> EntitlementSnapshot {
        gateway.fetch(subjectId: subjectId)
    }
}
