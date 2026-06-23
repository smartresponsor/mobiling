import SwiftUI

struct AccessEntryFormView<Content: View>: View {
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
                VStack(alignment: .leading, spacing: 10) {
                    Text("SmartResponsor")
                        .font(.largeTitle.weight(.semibold))
                    Text(title)
                        .font(.title2.weight(.semibold))
                    Text(subtitle)
                        .font(.body)
                        .foregroundStyle(.secondary)
                }

                VStack(alignment: .leading, spacing: 16) {
                    content
                    if let statusMessage {
                        Text(statusMessage)
                            .font(.callout)
                            .foregroundStyle(.accentColor)
                    }
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(Color(.secondarySystemBackground))
                )

                Button(primaryActionTitle, action: onPrimaryAction)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button(secondaryActionTitle, action: onSecondaryAction)
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Return to access welcome", action: onBack)
                    .buttonStyle(.plain)
            }
            .padding(24)
        }
        .background(Color(.systemBackground))
    }
}
