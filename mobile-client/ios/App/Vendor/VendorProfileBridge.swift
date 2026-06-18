import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-owned profile bridge.
struct VendorProfileBridge {
    let feature: ProfileFeatureBridge

    func detail(profileId: String) async -> ProfileDetailPayload {
        await feature.detail(profileId: profileId)
    }
}
