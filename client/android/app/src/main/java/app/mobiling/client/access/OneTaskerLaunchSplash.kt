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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

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
    var oneBaselineFraction by remember { mutableStateOf(1f) }
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
    val waveProgress by animateFloatAsState(
        targetValue = if (wordReady && motionEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "oneTaskerWordWaveProgress",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(splashAlpha)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Row {
            Text(
                text = "1",
                color = OneTaskerInk,
                fontSize = 72.sp,
                fontWeight = FontWeight.Medium,
                onTextLayout = { layoutResult ->
                    val height = layoutResult.size.height
                    if (height > 0) {
                        oneBaselineFraction = (layoutResult.firstBaseline / height).coerceIn(0f, 1f)
                    }
                },
                modifier = Modifier
                    .alpha(oneAlpha)
                    .alignByBaseline()
                    .graphicsLayer {
                        scaleX = 0.72f * oneScale
                        scaleY = 1.34f * oneScale
                        translationY = 2f
                        transformOrigin = TransformOrigin(0.5f, oneBaselineFraction)
                    },
            )
            Box(
                modifier = Modifier
                    .width(revealWidth)
                    .alignByBaseline()
                    .clipToBounds(),
            ) {
                Row(
                    modifier = Modifier
                        .alpha(wordAlpha)
                        .graphicsLayer {
                            scaleX = 0.92f
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        },
                ) {
                    "tasker.com".forEachIndexed { index, character ->
                        val isDomainSuffix = index >= 6
                        val waveOffset = if (motionEnabled && wordReady) {
                            val envelope = sin(PI * waveProgress).toFloat()
                            val phase = (waveProgress * 2f * PI.toFloat()) - (index * 0.55f)
                            (sin(phase) * envelope * 0.35f).dp
                        } else {
                            0.dp
                        }

                        Text(
                                text = character.toString(),
                                color = if (isDomainSuffix) OneTaskerInk.copy(alpha = 0.72f) else OneTaskerInk,
                                fontSize = when {
                                    index == 9 -> 35.sp
                                    isDomainSuffix -> 34.sp
                                    else -> 44.sp
                                },
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .offset(y = waveOffset),
                            )
                        }
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
