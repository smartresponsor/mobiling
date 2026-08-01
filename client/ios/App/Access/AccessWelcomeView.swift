import SwiftUI

struct AccessWelcomeView: View {
    let onSignIn: () -> Void
    let onCreateAccess: () -> Void

    private let businessAreas = ["Vendor", "Catalog", "Order", "Message"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 96), spacing: 8)], alignment: .leading, spacing: 8) {
                    ForEach(businessAreas, id: \.self) { area in
                        Text(area)
                            .font(.callout.weight(.semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(
                                Capsule(style: .continuous)
                                    .fill(Color(.systemBackground))
                            )
                    }
                }

                Button("Sign in", action: onSignIn)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Create access", action: onCreateAccess)
                    .buttonStyle(.bordered)
                    .tint(Color(red: 51 / 255, green: 51 / 255, blue: 51 / 255))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(24)
        }
        .background(Color(.systemGroupedBackground))
    }
}
