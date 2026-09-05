import SwiftUI

struct AccessFlowShellView<Content: View>: View {
    let title: String
    let subtitle: String
    let primaryActionTitle: String
    let secondaryActionTitle: String
    let onPrimaryAction: () -> Void
    let onSecondaryAction: () -> Void
    let onBack: () -> Void
    let statusMessage: String?
    private let content: Content

    init(
        title: String,
        subtitle: String,
        primaryActionTitle: String,
        secondaryActionTitle: String,
        onPrimaryAction: @escaping () -> Void,
        onSecondaryAction: @escaping () -> Void,
        onBack: @escaping () -> Void,
        statusMessage: String?,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.subtitle = subtitle
        self.primaryActionTitle = primaryActionTitle
        self.secondaryActionTitle = secondaryActionTitle
        self.onPrimaryAction = onPrimaryAction
        self.onSecondaryAction = onSecondaryAction
        self.onBack = onBack
        self.statusMessage = statusMessage
        self.content = content()
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                HStack(spacing: 12) {
                    Button(action: onBack) {
                        Image(systemName: "chevron.backward")
                            .font(.headline)
                    }
                    .accessibilityLabel("Back")
                    Text(title)
                        .font(.headline.weight(.semibold))
                    Spacer()
                }

                VStack(alignment: .leading, spacing: 16) {
                    content
                    if let statusMessage {
                        Text(statusMessage)
                            .font(.callout)
                            .foregroundStyle(Color.accentColor)
                    }
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(Color(.systemBackground))
                )

                Button(primaryActionTitle, action: onPrimaryAction)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button(secondaryActionTitle, action: onSecondaryAction)
                    .buttonStyle(.bordered)
                    .tint(Color.accentColor)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Return to access welcome", action: onBack)
                    .buttonStyle(.plain)
            }
            .padding(24)
        }
        .background(Color(.systemGroupedBackground))
    }
}
