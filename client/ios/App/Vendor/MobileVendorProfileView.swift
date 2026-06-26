import SwiftUI

public struct MobileVendorProfileView: View {
    private let vendorId: String?
    private let vendorProfileGateway: VendorProfileGateway?

    @State private var profile: MobileVendorProfileScreenContract?
    @State private var errorMessage: String?

    public init(vendorId: String?, vendorProfileGateway: VendorProfileGateway?) {
        self.vendorId = vendorId
        self.vendorProfileGateway = vendorProfileGateway
    }

    public var body: some View {
        List {
            Section {
                Text("My Profile")
                    .font(.headline)
            }

            if let errorMessage {
                Section {
                    Text(errorMessage)
                }
            } else if let profile {
                Section("Identity") {
                    field("Vendor ID", profile.vendorId)
                    field("Display", profile.title)
                    field("Brand", profile.brandName)
                    field("Status", profile.status)
                    field("Publication", profile.publicationStatus)
                }

                Section("Readiness") {
                    ProgressView(value: Double(profile.completionPercent), total: 100)
                    Text("Completion: \(profile.completionPercent)%")
                    Text(profile.readyForPublishing ? "Ready for publishing" : "Not ready yet")
                    field("Next action", profile.nextAction)
                }

                Section("Public profile") {
                    field("About", profile.about)
                    field("Website", profile.website)
                    field("Avatar URL", profile.avatarUrl)
                    field("Cover URL", profile.coverUrl)
                }
            } else {
                Section {
                    Text("Loading vendor profile...")
                }
            }
        }
        .navigationTitle("My Profile")
        .task(id: vendorId ?? "") {
            await load()
        }
    }

    @ViewBuilder
    private func field(_ label: String, _ value: String?) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value?.isEmpty == false ? value! : "—")
        }
    }

    private func load() async {
        profile = nil
        errorMessage = nil

        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Profile requires an active vendor session."
            return
        }

        guard let vendorProfileGateway else {
            errorMessage = "Vendor profile gateway is not available."
            return
        }

        do {
            let payload = try await LoadVendorProfileUseCase(gateway: vendorProfileGateway)(vendorId: vendorId)
            profile = MobileVendorProfileScreenContract(payload: payload)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
