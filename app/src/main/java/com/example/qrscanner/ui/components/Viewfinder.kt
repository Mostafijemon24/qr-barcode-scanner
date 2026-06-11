package com.example.qrscanner.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.qrscanner.ui.theme.AppColors

/**
 * The signature scan viewfinder: four white corner brackets, a faint "ghost" QR
 * fill, and a red laser line that sweeps top-to-bottom — mirroring the mock.
 */
@Composable
fun Viewfinder(modifier: Modifier = Modifier, showGhost: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "laser")
    val frac by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "laserFrac",
    )

    BoxWithConstraints(modifier.size(228.dp)) {
        val h = maxHeight

        Canvas(Modifier.size(228.dp)) {
            val s = size.minDimension
            // --- faint ghost QR checkerboard, inset like the mock (hidden over live camera) ---
            if (showGhost) {
                val inset = 26.dp.toPx()
                val cells = 9
                val cell = (s - inset * 2) / cells
                for (r in 0 until cells) {
                    for (c in 0 until cells) {
                        if ((r + c) % 2 == 0) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.16f),
                                topLeft = Offset(inset + c * cell, inset + r * cell),
                                size = androidx.compose.ui.geometry.Size(cell, cell),
                            )
                        }
                    }
                }
            }

            // --- corner brackets ---
            val sw = 3.dp.toPx()
            val arm = 34.dp.toPx()
            val rad = 6.dp.toPx()
            val p = sw / 2
            val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val col = AppColors.FinderStroke

            // top-left
            drawPath(Path().apply {
                moveTo(p, arm); lineTo(p, p + rad)
                quadraticTo(p, p, p + rad, p); lineTo(arm, p)
            }, col, style = stroke)
            // top-right
            drawPath(Path().apply {
                moveTo(s - p, arm); lineTo(s - p, p + rad)
                quadraticTo(s - p, p, s - p - rad, p); lineTo(s - arm, p)
            }, col, style = stroke)
            // bottom-left
            drawPath(Path().apply {
                moveTo(p, s - arm); lineTo(p, s - p - rad)
                quadraticTo(p, s - p, p + rad, s - p); lineTo(arm, s - p)
            }, col, style = stroke)
            // bottom-right
            drawPath(Path().apply {
                moveTo(s - p, s - arm); lineTo(s - p, s - p - rad)
                quadraticTo(s - p, s - p, s - p - rad, s - p); lineTo(s - arm, s - p)
            }, col, style = stroke)
        }

        // --- laser sweep ---
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .offset(y = h * frac - 7.dp)
                .height(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            // soft glow
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.5f to AppColors.Laser.copy(alpha = 0.40f),
                            1f to Color.Transparent,
                        ),
                        RoundedCornerShape(50),
                    )
            )
            // crisp line
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.18f to AppColors.Laser,
                            0.5f to AppColors.LaserSoft,
                            0.82f to AppColors.Laser,
                            1f to Color.Transparent,
                        )
                    )
            )
        }
    }
}
