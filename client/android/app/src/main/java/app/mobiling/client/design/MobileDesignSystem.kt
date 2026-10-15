package app.mobiling.client.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MobileSpacingMetrics(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
)

@Immutable
data class MobileNavigationMetrics(
    val activeColor: Color,
    val activeIndicatorHeight: Dp,
)

@Immutable
data class MobileMessageComposerMetrics(
    val innerInset: Dp,
    val actionGap: Dp,
    val outerGap: Dp,
    val actionSize: Dp,
    val clearSize: Dp,
    val sendSize: Dp,
)

object MobileDesignDefaults {
    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
    }

    object Control {
        val compact = 36.dp
        val regular = 44.dp
        val prominent = 48.dp
    }

    val spacing = MobileSpacingMetrics(
        xs = Spacing.xs,
        sm = Spacing.sm,
        md = Spacing.md,
        lg = Spacing.lg,
        xl = Spacing.xl,
        xxl = Spacing.xxl,
    )

    object MessageBubble {
        val horizontalInset = 14.dp
        val verticalInset = 10.dp
        val maxWidth = 310.dp
    }

    object MessageTimeline {
        val topInset = 14.dp
        val bottomInset = 104.dp
        val avatarSize = Control.regular
    }

    object Attachment {
        val gridMinCellWidth = 150.dp
        val browserHeight = 520.dp
        val gridGap = 10.dp
        val detailPreviewHeight = 360.dp
        val cardPreviewHeight = 130.dp
        val cardInset = 10.dp
        val placeholderTopInset = 42.dp
    }

    object Dashboard {
        val iconInset = 10.dp
    }

    object Access {
        val loadingIndicatorSize = Spacing.xl
        val loadingIndicatorStrokeWidth = 2.dp
        val passwordQualityGap = 6.dp
    }

    object Notification {
        val rowGap = 6.dp
    }

    val navigationIndicatorHeight = Spacing.xs

    val messageComposer = MobileMessageComposerMetrics(
        innerInset = Spacing.xs,
        actionGap = Spacing.xs,
        outerGap = Spacing.sm,
        actionSize = Control.regular,
        clearSize = Control.compact,
        sendSize = Control.prominent,
    )
}

private val LocalSpacingMetrics = staticCompositionLocalOf { MobileDesignDefaults.spacing }

private val LocalNavigationMetrics = staticCompositionLocalOf {
    MobileNavigationMetrics(
        activeColor = Color.Unspecified,
        activeIndicatorHeight = MobileDesignDefaults.navigationIndicatorHeight,
    )
}

private val LocalMessageComposerMetrics = staticCompositionLocalOf { MobileDesignDefaults.messageComposer }

object MobileDesignSystem {
    val spacing: MobileSpacingMetrics
        @Composable get() = LocalSpacingMetrics.current
    val navigation: MobileNavigationMetrics
        @Composable get() = LocalNavigationMetrics.current
    val messageComposer: MobileMessageComposerMetrics
        @Composable get() = LocalMessageComposerMetrics.current
}

@Composable
fun ProvideMobileDesignSystem(
    spacing: MobileSpacingMetrics,
    navigation: MobileNavigationMetrics,
    messageComposer: MobileMessageComposerMetrics,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacingMetrics provides spacing,
        LocalNavigationMetrics provides navigation,
        LocalMessageComposerMetrics provides messageComposer,
        content = content,
    )
}
