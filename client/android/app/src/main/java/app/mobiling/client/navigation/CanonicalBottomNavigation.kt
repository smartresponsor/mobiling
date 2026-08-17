package app.mobiling.client.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.mobiling.client.design.OneTaskerDesignTokens

private object NoNavigationRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = Color.Transparent

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0f, 0f, 0f, 0f)
}

data class CanonicalBottomNavigationItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun CanonicalBottomNavigation(items: List<CanonicalBottomNavigationItem>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val selectedIndex = items.indexOfFirst { it.selected }
        val itemWidth = if (items.isNotEmpty()) maxWidth / items.size else 0.dp
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedIndex >= 0) itemWidth * selectedIndex else 0.dp,
            label = "bottom-navigation-indicator",
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                CompositionLocalProvider(LocalRippleTheme provides NoNavigationRippleTheme) {
                    NavigationBarItem(
                        selected = item.selected,
                        onClick = item.onClick,
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        enabled = item.enabled,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OneTaskerDesignTokens.Navigation.ActiveColor,
                            selectedTextColor = OneTaskerDesignTokens.Navigation.ActiveColor,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }

        if (selectedIndex >= 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .height(OneTaskerDesignTokens.Navigation.ActiveIndicatorHeight)
                    .background(OneTaskerDesignTokens.Navigation.ActiveColor),
            )
        }
    }
}
