package app.mobiling.client.consumer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OneTaskerLightColorScheme = lightColorScheme(
    primary = Color(0xFFF96302),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2D1),
    onPrimaryContainer = Color(0xFF3D1800),
    secondary = Color(0xFF333333),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Color(0xFF666666),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2F2F2),
    onTertiaryContainer = Color(0xFF333333),
    background = Color.White,
    onBackground = Color(0xFF333333),
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFF5F5F5),
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
        content = content,
    )
}
