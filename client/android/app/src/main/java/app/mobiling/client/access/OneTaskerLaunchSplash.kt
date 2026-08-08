package app.mobiling.client.access

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val OneTaskerInk = Color(0xFF2D2F34)

@Composable
fun OneTaskerLaunchSplash(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val motionEnabled = remember(context) { oneTaskerMotionEnabled(context) }
    var oneReady by remember { mutableStateOf(!motionEnabled) }
    var wordReady by remember { mutableStateOf(!motionEnabled) }
    val wordWidth = 214.dp

    LaunchedEffect(motionEnabled) {
        if (!motionEnabled) {
            oneReady = true
            wordReady = true
            return@LaunchedEffect
        }

        delay(90)
        oneReady = true
        delay(390)
        wordReady = true
    }

    val splashAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (motionEnabled) 280 else 80, easing = LinearEasing),
        label = "oneTaskerSplashAlpha",
    )
    val oneAlpha by animateFloatAsState(
        targetValue = if (oneReady) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "oneTaskerOneAlpha",
    )
    val oneScale by animateFloatAsState(
        targetValue = if (oneReady) 1f else 0.92f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "oneTaskerOneScale",
    )
    val revealWidth by animateDpAsState(
        targetValue = if (wordReady) wordWidth else 0.dp,
        animationSpec = tween(durationMillis = if (motionEnabled) 620 else 80, easing = FastOutSlowInEasing),
        label = "oneTaskerRevealWidth",
    )
    val wordAlpha by animateFloatAsState(
        targetValue = if (wordReady) 1f else 0f,
        animationSpec = tween(durationMillis = if (motionEnabled) 240 else 80, easing = LinearEasing),
        label = "oneTaskerWordAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(splashAlpha)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "1",
                color = OneTaskerInk,
                fontSize = 72.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(oneAlpha)
                    .scale(scaleX = 0.72f * oneScale, scaleY = 1.34f * oneScale),
            )
            Box(
                modifier = Modifier
                    .width(revealWidth)
                    .clipToBounds(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(wordAlpha),
                ) {
                    Text(
                        text = "tasker",
                        color = OneTaskerInk,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                    )
                    Text(
                        text = ".com",
                        color = OneTaskerInk.copy(alpha = 0.46f),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

internal fun oneTaskerMotionEnabled(context: Context): Boolean = try {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
} catch (_: Exception) {
    true
}
