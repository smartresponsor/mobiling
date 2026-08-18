import SwiftUI

struct MobileMessageComposerMetrics: Sendable {
    let innerInset: CGFloat
    let actionGap: CGFloat
    let outerGap: CGFloat
    let actionSize: CGFloat
    let clearSize: CGFloat
    let sendSize: CGFloat
}

enum MobileDesignDefaults {
    enum Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 20
        static let xxl: CGFloat = 24
    }

    enum Control {
        static let compact: CGFloat = 36
        static let regular: CGFloat = 44
        static let prominent: CGFloat = 48
    }

    enum MessageBubble {
        static let horizontalInset: CGFloat = 14
        static let verticalInset: CGFloat = 10
        static let maxWidth: CGFloat = 310
    }

    enum Attachment {
        static let gridMinCellWidth: CGFloat = 150
        static let detailPreviewMinHeight: CGFloat = 260
        static let cardPreviewHeight: CGFloat = 120
        static let cardInset: CGFloat = 10
        static let cardRadius: CGFloat = 14
        static let previewRadius: CGFloat = 12
    }

    enum Access {
        static let passwordQualityGap: CGFloat = 6
    }

    enum Notification {
        static let rowGap: CGFloat = 6
    }

    static let messageComposer = MobileMessageComposerMetrics(
        innerInset: Spacing.xs,
        actionGap: Spacing.xs,
        outerGap: Spacing.sm,
        actionSize: Control.regular,
        clearSize: Control.compact,
        sendSize: Control.prominent
    )
}

private struct MobileMessageComposerMetricsKey: EnvironmentKey {
    static let defaultValue = MobileDesignDefaults.messageComposer
}

extension EnvironmentValues {
    var mobileMessageComposer: MobileMessageComposerMetrics {
        get { self[MobileMessageComposerMetricsKey.self] }
        set { self[MobileMessageComposerMetricsKey.self] = newValue }
    }
}

struct CanonicalMessageBubble: View {
    let body: String
    let timestamp: String
    let ownMessage: Bool

    var body: some View {
        HStack {
            if ownMessage { Spacer(minLength: MobileDesignDefaults.Control.prominent) }
            VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.xs) {
                Text(body)
                    .font(.body)
                Text(timestamp)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            .padding(.horizontal, MobileDesignDefaults.MessageBubble.horizontalInset)
            .padding(.vertical, MobileDesignDefaults.MessageBubble.verticalInset)
            .frame(maxWidth: MobileDesignDefaults.MessageBubble.maxWidth, alignment: .leading)
            .background(.quaternary, in: RoundedRectangle(cornerRadius: MobileDesignDefaults.Spacing.lg, style: .continuous))
            if !ownMessage { Spacer(minLength: MobileDesignDefaults.Control.prominent) }
        }
    }
}

struct CanonicalStateCard: View {
    let title: String
    let description: String

    var body: some View {
        VStack(alignment: .leading, spacing: MobileDesignDefaults.Spacing.sm) {
            Text(title).font(.headline)
            Text(description).font(.body).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, MobileDesignDefaults.Spacing.xl)
        .padding(.vertical, MobileDesignDefaults.Spacing.xxl)
        .background(.quaternary, in: RoundedRectangle(cornerRadius: MobileDesignDefaults.Spacing.xl, style: .continuous))
    }
}

struct CanonicalMessageComposer: View {
    @Environment(\.mobileMessageComposer) private var metrics
    @Binding var draft: String
    let sending: Bool
    let onAttach: () -> Void
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: metrics.outerGap) {
            HStack(spacing: metrics.actionGap) {
                Button(action: onAttach) {
                    Image(systemName: "plus")
                        .frame(width: metrics.actionSize, height: metrics.actionSize)
                        .background(.quaternary, in: Circle())
                }
                .buttonStyle(.plain)

                TextField("Message", text: $draft)
                    .submitLabel(.send)
                    .onSubmit { if canSend { onSend() } }

                if !draft.isEmpty {
                    Button { draft = "" } label: {
                        Image(systemName: "xmark")
                            .frame(width: metrics.clearSize, height: metrics.clearSize)
                            .background(.quaternary, in: Circle())
                    }
                    .buttonStyle(.plain)
                }

                Button {} label: {
                    Image(systemName: "mic")
                        .frame(width: metrics.actionSize, height: metrics.actionSize)
                        .background(.quaternary, in: Circle())
                }
                .buttonStyle(.plain)
                .disabled(true)
            }
            .padding(.horizontal, metrics.innerInset)
            .padding(.vertical, metrics.innerInset)
            .background(.quaternary, in: Capsule())

            Button(action: onSend) {
                Image(systemName: "paperplane.fill")
                    .foregroundStyle(.white)
                    .frame(width: metrics.sendSize, height: metrics.sendSize)
                    .background(Color.accentColor, in: Circle())
            }
            .buttonStyle(.plain)
            .disabled(!canSend)
        }
    }

    private var canSend: Bool {
        !draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !sending
    }
}
