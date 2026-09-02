package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

val KurdistanRed = Color(0xFFE41E26)
val KurdistanWhite = Color(0xFFFFFFFF)
val KurdistanGreen = Color(0xFF00843D)
val KurdistanSunYellow = Color(0xFFFEB81C)

/**
 * High-precision Kurdistan National Flag Composable with official 21-ray golden sun
 */
@Composable
fun KurdistanFlag(
    modifier: Modifier = Modifier,
    width: Dp = 36.dp,
    height: Dp = 24.dp,
    cornerRadius: Dp = 4.dp,
    elevation: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .border(0.5.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(cornerRadius))
            .width(width)
            .height(height)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val stripeH = h / 3f

            // Top Red Stripe
            drawRect(
                color = KurdistanRed,
                topLeft = Offset(0f, 0f),
                size = Size(w, stripeH)
            )

            // Middle White Stripe
            drawRect(
                color = KurdistanWhite,
                topLeft = Offset(0f, stripeH),
                size = Size(w, stripeH)
            )

            // Bottom Green Stripe
            drawRect(
                color = KurdistanGreen,
                topLeft = Offset(0f, stripeH * 2f),
                size = Size(w, stripeH)
            )

            // Center Golden Sun with 21 Rays
            val cx = w / 2f
            val cy = h / 2f
            val centerRadius = stripeH * 0.32f
            val rayOuterRadius = stripeH * 0.48f
            val rayInnerRadius = centerRadius * 0.95f
            val totalRays = 21
            val angleStep = (2.0 * Math.PI) / totalRays

            // Draw 21 sun rays
            for (i in 0 until totalRays) {
                val tipAngle = i * angleStep - (Math.PI / 2.0)
                val baseAngle1 = tipAngle - (angleStep * 0.4)
                val baseAngle2 = tipAngle + (angleStep * 0.4)

                val tipX = cx + (rayOuterRadius * cos(tipAngle)).toFloat()
                val tipY = cy + (rayOuterRadius * sin(tipAngle)).toFloat()

                val b1X = cx + (rayInnerRadius * cos(baseAngle1)).toFloat()
                val b1Y = cy + (rayInnerRadius * sin(baseAngle1)).toFloat()

                val b2X = cx + (rayInnerRadius * cos(baseAngle2)).toFloat()
                val b2Y = cy + (rayInnerRadius * sin(baseAngle2)).toFloat()

                val rayPath = Path().apply {
                    moveTo(b1X, b1Y)
                    lineTo(tipX, tipY)
                    lineTo(b2X, b2Y)
                    close()
                }

                drawPath(
                    path = rayPath,
                    color = KurdistanSunYellow
                )
            }

            // Draw central sun disk
            drawCircle(
                color = KurdistanSunYellow,
                radius = centerRadius,
                center = Offset(cx, cy)
            )
        }
    }
}
