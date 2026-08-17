package app.mobiling.client.consumer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mobiling.client.design.OneTaskerDesignTokens

private val OneTaskerShapes = Shapes(
    extraSmall = RoundedCornerShape(OneTaskerDesignTokens.Spacing.Xs),
    small = RoundedCornerShape(OneTaskerDesignTokens.Spacing.Sm),
    medium = RoundedCornerShape(OneTaskerDesignTokens.Spacing.Md),
    large = RoundedCornerShape(OneTaskerDesignTokens.Spacing.Lg),
    extraLarge = RoundedCornerShape(28.dp),
)

private val OneTaskerLightColorScheme = lightColorScheme(
    primary = Color(0xFFF96302),
    onPrimary = Color.White,
    primaryContainer = Color.White,
    onPrimaryContainer = Color(0xFF333333),
    secondary = Color(0xFF333333),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F0F0),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Color(0xFF666666),
    onTertiary = Color.White,
    tertiaryContainer = Color.White,
    onTertiaryContainer = Color(0xFF333333),
    background = Color(0xFFF4F5F6),
    onBackground = Color(0xFF333333),
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color.White,
    surfaceTint = Color.Transparent,
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFF767676),
    outlineVariant = Color(0xFFD6D6D6),
    error = Color(0xFFB00020),
    onError = Color.White,
)

@Composable
fun OneTaskerTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = OneTaskerLightColorScheme,
        shapes = OneTaskerShapes,
        content = content,
    )
}
