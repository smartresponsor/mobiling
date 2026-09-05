import SwiftUI

enum OneTaskerDesignTokens {
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

    static let messageComposer = MobileMessageComposerMetrics(
        innerInset: Spacing.xs,
        actionGap: Spacing.xs,
        outerGap: Spacing.sm,
        actionSize: Control.regular,
        clearSize: Control.compact,
        sendSize: Control.prominent
    )
}
