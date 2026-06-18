import MobileClient
import Foundation

public actor Entitlement {
  private let refreshEntitlementSnapshotUseCase: RefreshEntitlementSnapshotUseCase
  private let entitlementSnapshotGateway: EntitlementSnapshotGateway

  public init(
    entitlementSnapshotGateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway()
  ) {
    self.entitlementSnapshotGateway = entitlementSnapshotGateway
    self.refreshEntitlementSnapshotUseCase = RefreshEntitlementSnapshotUseCase(gateway: entitlementSnapshotGateway)
  }

  public func isEntitled(feature: String, subjectId: String = "local") async -> Bool {
    let snapshot = refreshEntitlementSnapshotUseCase(subjectId: subjectId)
    return snapshot.grantedCodes.contains(feature)
  }

  public func refresh(subjectId: String = "local") async -> EntitlementSnapshot {
    refreshEntitlementSnapshotUseCase(subjectId: subjectId)
  }

  public func snapshot(subjectId: String = "local") async -> EntitlementSnapshot {
    refreshEntitlementSnapshotUseCase(subjectId: subjectId)
  }

  public func grant(feature: String, subjectId: String = "local") async -> EntitlementSnapshot {
    let baseSnapshot = refreshEntitlementSnapshotUseCase(subjectId: subjectId)
    if baseSnapshot.grantedCodes.contains(feature) {
      return baseSnapshot
    }

    return EntitlementSnapshot(
      subjectId: baseSnapshot.subjectId,
      grantedCodes: baseSnapshot.grantedCodes + [feature],
      refreshedAtIso8601: baseSnapshot.refreshedAtIso8601
    )
  }
}
