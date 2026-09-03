package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCobalt
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorWaveform(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 200.dp,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    isThinking: Boolean = false,
    isGeminiLive: Boolean = false,
    voiceAmplitude: Float = 0.15f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    // Dynamic rotation speed
    val rotationDuration = when {
        isThinking -> 1200
        isSpeaking || isListening -> 3500
        else -> 16000
    }

    // Slow continuous idle rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Reverse ring rotation
    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 1800 else if (isSpeaking || isListening) 5500 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    // Pulsing energy core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = if (isThinking) 1.22f else if (isSpeaking || isListening) 1.15f else 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 400 else if (isSpeaking || isListening) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Dynamic color based on state
    val coreColor = when {
        isThinking -> com.example.ui.theme.JarvisPurpleLight // Luminous neon purple for Gemini Live synthesis
        isListening -> JarvisCyanLight
        isSpeaking -> com.example.ui.theme.JarvisPurple
        isGeminiLive -> JarvisCyan
        else -> JarvisCyan
    }

    val glowColor = when {
        isThinking -> com.example.ui.theme.JarvisPurpleGlow
        isListening -> JarvisCyan.copy(alpha = 0.45f)
        isSpeaking -> com.example.ui.theme.JarvisPurple.copy(alpha = 0.5f)
        else -> JarvisCyanGlow
    }

    // Dynamic amplitude influence
    val ampBoost = (voiceAmplitude * 0.22f)
    val effectivePulse = pulseScale + (if (isListening || isSpeaking) ampBoost else 0f)

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = false, radius = sizeDp / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = (this.size.minDimension / 2) * 0.92f

            // 1. Ambient Glow radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * effectivePulse
                ),
                radius = radius * effectivePulse,
                center = center
            )

            // 2. Outer segmented HUD Ring
            rotate(rotationAngle, pivot = center) {
                val segmentCount = 12
                val sweep = 360f / segmentCount
                for (i in 0 until segmentCount) {
                    val startAngle = i * sweep
                    val isGap = i % 3 == 0
                    if (!isGap) {
                        drawArc(
                            color = coreColor.copy(alpha = 0.45f),
                            startAngle = startAngle + 4f,
                            sweepAngle = sweep - 8f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // 3. Middle counter-rotating ring with notch ticks
            val midRadius = radius * 0.76f
            rotate(reverseRotationAngle, pivot = center) {
                drawCircle(
                    color = JarvisCobalt.copy(alpha = 0.35f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angleRad = Math.toRadians((i * (360.0 / tickCount)))
                    val p1 = Offset(
                        center.x + (midRadius - 4.dp.toPx()) * cos(angleRad).toFloat(),
                        center.y + (midRadius - 4.dp.toPx()) * sin(angleRad).toFloat()
                    )
                    val p2 = Offset(
                        center.x + (midRadius + 4.dp.toPx()) * cos(angleRad).toFloat(),
                        center.y + (midRadius + 4.dp.toPx()) * sin(angleRad).toFloat()
                    )
                    drawLine(
                        color = coreColor.copy(alpha = if (i % 6 == 0) 0.85f else 0.35f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (i % 6 == 0) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 4. Inner Arc Reactor Ring with Triangular Nodes
            val innerRadius = radius * 0.52f * effectivePulse
            drawCircle(
                color = coreColor.copy(alpha = 0.75f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            rotate(rotationAngle * 1.5f, pivot = center) {
                val nodeCount = 3
                for (i in 0 until nodeCount) {
                    val angleRad = Math.toRadians((i * (360.0 / nodeCount)) - 90.0)
                    val nodeCenter = Offset(
                        center.x + innerRadius * cos(angleRad).toFloat(),
                        center.y + innerRadius * sin(angleRad).toFloat()
                    )
                    drawCircle(
                        color = coreColor,
                        radius = 4.5.dp.toPx(),
                        center = nodeCenter
                    )
                }
            }

            // 5. Central Glowing AI Processor Core
            val centerCoreRadius = radius * 0.26f * effectivePulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, coreColor, coreColor.copy(alpha = 0.1f)),
                    center = center,
                    radius = centerCoreRadius
                ),
                radius = centerCoreRadius,
                center = center
            )
        }
    }
}
