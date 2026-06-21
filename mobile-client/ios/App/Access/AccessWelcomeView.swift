import SwiftUI

struct AccessWelcomeView: View {
    let onSignIn: () -> Void
    let onCreateAccess: () -> Void

    private let businessAreas = ["Vendor", "Catalog", "Order", "Message"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("SmartResponsor")
                    .font(.largeTitle.weight(.semibold))
                Text("Business access for vendor, catalog, order, and message workflows.")
                    .font(.title3)
                Text("Access is required to open the business workspace.")
                    .foregroundStyle(.secondary)

                LazyVGrid(columns: [GridItem(.adaptive(minimum: 96), spacing: 8)], alignment: .leading, spacing: 8) {
                    ForEach(businessAreas, id: \.self) { area in
                        Text(area)
                            .font(.callout.weight(.semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(
                                Capsule(style: .continuous)
                                    .fill(Color(.secondarySystemBackground))
                            )
                    }
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("Guest entry")
                        .font(.headline)
                    Text("Sign in to continue or create access for a new workspace account.")
                        .foregroundStyle(.secondary)
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(Color(.secondarySystemBackground))
                )

                Button("Sign in", action: onSignIn)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Create access", action: onCreateAccess)
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(24)
        }
        .background(Color(.systemBackground))
    }
}
