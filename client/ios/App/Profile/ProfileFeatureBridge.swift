import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// App-level bridge for profile-domain controlled rewire.
@available(*, deprecated, message: "Prefer VendorBusinessBridge.ownedProfile() or VendorNavigationBridge.profile for vendor-owned navigation.")
struct ProfileFeatureBridge {
    let detailGateway: ProfileDetailGateway

    func detail(profileId: String) async -> ProfileDetailPayload {
        await LoadProfileDetailUseCase(gateway: detailGateway).invoke(profileId: profileId)
    }
}
