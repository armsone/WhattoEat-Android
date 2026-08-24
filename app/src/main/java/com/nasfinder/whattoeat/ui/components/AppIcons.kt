package com.nasfinder.whattoeat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.theme.CharcoalText

@Composable
fun IconHome(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.90f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.88f)
            lineTo(w * 0.58f, h * 0.88f)
            lineTo(w * 0.58f, h * 0.60f)
            lineTo(w * 0.42f, h * 0.60f)
            lineTo(w * 0.42f, h * 0.88f)
            lineTo(w * 0.20f, h * 0.88f)
            lineTo(w * 0.20f, h * 0.44f)
            lineTo(w * 0.10f, h * 0.44f)
            close()
        }
        drawPath(path, color = tint, style = Fill)
    }
}

@Composable
fun IconMap(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // `map.fill`: three optically balanced folded panels with clean creases.
        val leftPanel = Path().apply {
            moveTo(w * 0.09f, h * 0.24f)
            cubicTo(w * 0.09f, h * 0.20f, w * 0.11f, h * 0.18f, w * 0.15f, h * 0.17f)
            lineTo(w * 0.355f, h * 0.105f)
            lineTo(w * 0.355f, h * 0.785f)
            lineTo(w * 0.13f, h * 0.86f)
            cubicTo(w * 0.105f, h * 0.87f, w * 0.09f, h * 0.85f, w * 0.09f, h * 0.82f)
            close()
        }
        val centerPanel = Path().apply {
            moveTo(w * 0.385f, h * 0.105f)
            lineTo(w * 0.625f, h * 0.205f)
            lineTo(w * 0.625f, h * 0.895f)
            lineTo(w * 0.385f, h * 0.785f)
            close()
        }
        val rightPanel = Path().apply {
            moveTo(w * 0.655f, h * 0.205f)
            lineTo(w * 0.87f, h * 0.13f)
            cubicTo(w * 0.905f, h * 0.12f, w * 0.925f, h * 0.14f, w * 0.925f, h * 0.175f)
            lineTo(w * 0.925f, h * 0.79f)
            cubicTo(w * 0.925f, h * 0.83f, w * 0.905f, h * 0.85f, w * 0.87f, h * 0.86f)
            lineTo(w * 0.655f, h * 0.895f)
            close()
        }
        drawPath(leftPanel, color = tint, style = Fill)
        drawPath(centerPanel, color = tint, style = Fill)
        drawPath(rightPanel, color = tint, style = Fill)
    }
}

@Composable
fun IconDice5(
    modifier: Modifier = Modifier.size(21.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = w * 0.22f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f),
            cornerRadius = CornerRadius(radius, radius)
        )
        // 5 white dots
        val dotRadius = w * 0.07f
        val dotColor = Color.White
        drawCircle(dotColor, radius = dotRadius, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(dotColor, radius = dotRadius, center = Offset(w * 0.30f, h * 0.30f))
        drawCircle(dotColor, radius = dotRadius, center = Offset(w * 0.70f, h * 0.30f))
        drawCircle(dotColor, radius = dotRadius, center = Offset(w * 0.30f, h * 0.70f))
        drawCircle(dotColor, radius = dotRadius, center = Offset(w * 0.70f, h * 0.70f))
    }
}

@Composable
fun IconHistory(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.095f
        // `clock.arrow.circlepath`: open circular arrow with a compact filled head.
        drawArc(
            color = tint,
            startAngle = -64f,
            sweepAngle = 286f,
            useCenter = false,
            topLeft = Offset(w * 0.135f, h * 0.135f),
            size = Size(w * 0.73f, h * 0.73f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // The arrowhead sits at the lower-left end of the counter-clockwise path.
        val arrowPath = Path().apply {
            moveTo(w * 0.135f, h * 0.64f)
            lineTo(w * 0.085f, h * 0.815f)
            lineTo(w * 0.265f, h * 0.765f)
            close()
        }
        drawPath(arrowPath, color = tint, style = Fill)
        // Clock hands
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.305f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.665f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconHeart(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText,
    isFilled: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(w * 0.15f, h * 0.60f, w * 0.05f, h * 0.35f, w * 0.15f, h * 0.20f)
            cubicTo(w * 0.25f, h * 0.05f, w * 0.42f, h * 0.12f, w * 0.5f, h * 0.28f)
            cubicTo(w * 0.58f, h * 0.12f, w * 0.75f, h * 0.05f, w * 0.85f, h * 0.20f)
            cubicTo(w * 0.95f, h * 0.35f, w * 0.85f, h * 0.60f, w * 0.5f, h * 0.85f)
            close()
        }
        if (isFilled) {
            drawPath(path, color = tint, style = Fill)
        } else {
            drawPath(path, color = tint, style = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun IconGear(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val outerRadius = w * 0.40f
        val innerRadius = w * 0.26f
        val holeRadius = w * 0.14f

        val teeth = 8
        val path = Path()
        for (i in 0 until teeth) {
            val angleDeg = (i * 360.0 / teeth).toFloat()
            val rad1 = Math.toRadians((angleDeg - 10).toDouble()).toFloat()
            val rad2 = Math.toRadians((angleDeg - 6).toDouble()).toFloat()
            val rad3 = Math.toRadians((angleDeg + 6).toDouble()).toFloat()
            val rad4 = Math.toRadians((angleDeg + 10).toDouble()).toFloat()

            val p1 = Offset(center.x + innerRadius * kotlin.math.cos(rad1), center.y + innerRadius * kotlin.math.sin(rad1))
            val p2 = Offset(center.x + outerRadius * kotlin.math.cos(rad2), center.y + outerRadius * kotlin.math.sin(rad2))
            val p3 = Offset(center.x + outerRadius * kotlin.math.cos(rad3), center.y + outerRadius * kotlin.math.sin(rad3))
            val p4 = Offset(center.x + innerRadius * kotlin.math.cos(rad4), center.y + innerRadius * kotlin.math.sin(rad4))

            if (i == 0) path.moveTo(p1.x, p1.y) else path.lineTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.lineTo(p3.x, p3.y)
            path.lineTo(p4.x, p4.y)
        }
        path.close()
        drawPath(path, color = tint, style = Fill)
        drawCircle(color = Color.White, radius = holeRadius, center = center)
    }
}

@Composable
fun IconChevronRight(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.20f)
            lineTo(w * 0.65f, h * 0.50f)
            lineTo(w * 0.35f, h * 0.80f)
        }
        drawPath(path, color = tint, style = Stroke(width = w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconChevronDown(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.20f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.65f)
            lineTo(w * 0.80f, h * 0.35f)
        }
        drawPath(path, color = tint, style = Stroke(width = w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconSearch(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        drawCircle(
            color = tint,
            radius = w * 0.28f,
            center = Offset(w * 0.42f, h * 0.42f),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.62f, h * 0.62f),
            end = Offset(w * 0.85f, h * 0.85f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconPin(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.90f)
            cubicTo(w * 0.30f, h * 0.65f, w * 0.18f, h * 0.48f, w * 0.18f, h * 0.35f)
            cubicTo(w * 0.18f, h * 0.16f, w * 0.32f, h * 0.08f, w * 0.5f, h * 0.08f)
            cubicTo(w * 0.68f, h * 0.08f, w * 0.82f, h * 0.16f, w * 0.82f, h * 0.35f)
            cubicTo(w * 0.82f, h * 0.48f, w * 0.70f, h * 0.65f, w * 0.5f, h * 0.90f)
            close()
        }
        drawPath(path, color = tint, style = Fill)
        drawCircle(color = Color.White, radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.35f))
    }
}

@Composable
fun IconTrash(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.10f
        // Can body
        val bodyPath = Path().apply {
            moveTo(w * 0.25f, h * 0.30f)
            lineTo(w * 0.30f, h * 0.85f)
            lineTo(w * 0.70f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.30f)
            close()
        }
        drawPath(bodyPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Lid
        drawLine(color = tint, start = Offset(w * 0.18f, h * 0.30f), end = Offset(w * 0.82f, h * 0.30f), strokeWidth = stroke, cap = StrokeCap.Round)
        // Handle
        val handlePath = Path().apply {
            moveTo(w * 0.38f, h * 0.30f)
            lineTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.62f, h * 0.18f)
            lineTo(w * 0.62f, h * 0.30f)
        }
        drawPath(handlePath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconCheckSeal(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val outerRadius = w * 0.44f
        val innerRadius = w * 0.36f

        val spikes = 12
        val path = Path()
        for (i in 0 until spikes * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 360.0 / (spikes * 2)).toDouble()).toFloat()
            val x = center.x + radius * kotlin.math.cos(angle)
            val y = center.y + radius * kotlin.math.sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = tint, style = Fill)

        // White checkmark
        val check = Path().apply {
            moveTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.64f)
            lineTo(w * 0.68f, h * 0.36f)
        }
        drawPath(check, color = Color.White, style = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconCheckCircle(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        drawCircle(color = tint, radius = size.minDimension * 0.46f)
        val check = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.51f)
            lineTo(size.width * 0.44f, size.height * 0.66f)
            lineTo(size.width * 0.71f, size.height * 0.34f)
        }
        drawPath(
            path = check,
            color = Color.White,
            style = Stroke(width = size.width * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconNavigationArrow(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.50f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(path, color = tint, style = Fill)
    }
}

@Composable
fun IconCrosshair(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.10f
        drawCircle(color = tint, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = stroke))
        drawLine(color = tint, start = Offset(w * 0.5f, h * 0.06f), end = Offset(w * 0.5f, h * 0.22f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.5f, h * 0.78f), end = Offset(w * 0.5f, h * 0.94f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.06f, h * 0.5f), end = Offset(w * 0.22f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.78f, h * 0.5f), end = Offset(w * 0.94f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconStar(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val outerRadius = w * 0.44f
        val innerRadius = w * 0.20f
        val points = 5
        val path = Path()
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 360.0 / (points * 2) - 90).toDouble()).toFloat()
            val x = center.x + radius * kotlin.math.cos(angle)
            val y = center.y + radius * kotlin.math.sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = tint, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconBell(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.10f
        val bellPath = Path().apply {
            moveTo(w * 0.50f, h * 0.15f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.22f, h * 0.35f, w * 0.22f, h * 0.65f)
            lineTo(w * 0.14f, h * 0.78f)
            lineTo(w * 0.86f, h * 0.78f)
            lineTo(w * 0.78f, h * 0.65f)
            cubicTo(w * 0.78f, h * 0.35f, w * 0.65f, h * 0.15f, w * 0.50f, h * 0.15f)
            close()
        }
        drawPath(bellPath, color = tint, style = Fill)
        drawCircle(color = tint, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.88f))
    }
}

@Composable
fun IconCamera(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.10f, h * 0.28f),
            size = Size(w * 0.80f, h * 0.58f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = stroke)
        )
        val bumpPath = Path().apply {
            moveTo(w * 0.36f, h * 0.28f)
            lineTo(w * 0.42f, h * 0.16f)
            lineTo(w * 0.58f, h * 0.16f)
            lineTo(w * 0.64f, h * 0.28f)
        }
        drawPath(bumpPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = tint, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.58f), style = Stroke(width = stroke))
    }
}

@Composable
fun IconInformation(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.13f
        drawCircle(color = tint, radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = stroke))
        drawCircle(color = tint, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.30f))
        drawLine(color = tint, start = Offset(w * 0.5f, h * 0.46f), end = Offset(w * 0.5f, h * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconPhoto(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.09f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.08f, size.height * 0.14f),
            size = Size(size.width * 0.84f, size.height * 0.72f),
            cornerRadius = CornerRadius(size.width * 0.12f),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(tint, radius = size.width * 0.075f, center = Offset(size.width * 0.68f, size.height * 0.36f))
        val mountain = Path().apply {
            moveTo(size.width * 0.17f, size.height * 0.73f)
            lineTo(size.width * 0.39f, size.height * 0.49f)
            lineTo(size.width * 0.54f, size.height * 0.63f)
            lineTo(size.width * 0.65f, size.height * 0.52f)
            lineTo(size.width * 0.83f, size.height * 0.73f)
        }
        drawPath(mountain, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconQuestionClock(
    modifier: Modifier = Modifier.size(18.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.11f
        drawCircle(color = tint, radius = w * 0.40f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = stroke))
        // Small question mark or hands
        drawLine(color = tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.5f, h * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.65f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconForkKnife(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.10f

        // Fork (Left)
        drawLine(color = tint, start = Offset(w * 0.30f, h * 0.50f), end = Offset(w * 0.30f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
        val forkBase = Path().apply {
            moveTo(w * 0.16f, h * 0.22f)
            lineTo(w * 0.16f, h * 0.38f)
            cubicTo(w * 0.16f, h * 0.52f, w * 0.44f, h * 0.52f, w * 0.44f, h * 0.38f)
            lineTo(w * 0.44f, h * 0.22f)
        }
        drawPath(forkBase, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color = tint, start = Offset(w * 0.30f, h * 0.22f), end = Offset(w * 0.30f, h * 0.45f), strokeWidth = stroke, cap = StrokeCap.Round)

        // Knife (Right)
        drawLine(color = tint, start = Offset(w * 0.70f, h * 0.52f), end = Offset(w * 0.70f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
        val knifeBlade = Path().apply {
            moveTo(w * 0.70f, h * 0.52f)
            lineTo(w * 0.70f, h * 0.14f)
            cubicTo(w * 0.86f, h * 0.20f, w * 0.86f, h * 0.42f, w * 0.70f, h * 0.52f)
            close()
        }
        drawPath(knifeBlade, color = tint, style = Fill)
    }
}

@Composable
fun IconTakeout(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f

        val bag = Path().apply {
            moveTo(w * 0.12f, h * 0.34f)
            lineTo(w * 0.18f, h * 0.88f)
            lineTo(w * 0.60f, h * 0.88f)
            lineTo(w * 0.66f, h * 0.34f)
            close()
        }
        drawPath(bag, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.23f, h * 0.34f), Offset(w * 0.27f, h * 0.18f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.55f, h * 0.34f), Offset(w * 0.51f, h * 0.18f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.27f, h * 0.18f), Offset(w * 0.51f, h * 0.18f), stroke, StrokeCap.Round)

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.70f, h * 0.40f),
            size = Size(w * 0.22f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.04f),
            style = Stroke(width = stroke)
        )
        drawLine(tint, Offset(w * 0.68f, h * 0.40f), Offset(w * 0.94f, h * 0.40f), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconCupSaucer(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f
        val cup = Path().apply {
            moveTo(w * 0.14f, h * 0.34f)
            lineTo(w * 0.20f, h * 0.70f)
            cubicTo(w * 0.25f, h * 0.82f, w * 0.58f, h * 0.82f, w * 0.63f, h * 0.70f)
            lineTo(w * 0.69f, h * 0.34f)
        }
        drawPath(cup, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawArc(
            color = tint,
            startAngle = -85f,
            sweepAngle = 170f,
            useCenter = false,
            topLeft = Offset(w * 0.58f, h * 0.40f),
            size = Size(w * 0.28f, h * 0.28f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(tint, Offset(w * 0.10f, h * 0.88f), Offset(w * 0.82f, h * 0.88f), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconLeaf(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f
        val leaf = Path().apply {
            moveTo(w * 0.17f, h * 0.78f)
            cubicTo(w * 0.17f, h * 0.33f, w * 0.48f, h * 0.12f, w * 0.88f, h * 0.12f)
            cubicTo(w * 0.88f, h * 0.55f, w * 0.62f, h * 0.84f, w * 0.17f, h * 0.78f)
            close()
        }
        drawPath(leaf, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.16f, h * 0.86f), Offset(w * 0.70f, h * 0.32f), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconStorefront(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f

        // Roof / Awning
        val awning = Path().apply {
            moveTo(w * 0.10f, h * 0.38f)
            lineTo(w * 0.20f, h * 0.18f)
            lineTo(w * 0.80f, h * 0.18f)
            lineTo(w * 0.90f, h * 0.38f)
            close()
        }
        drawPath(awning, color = tint, style = Fill)

        // Body / Walls
        val walls = Path().apply {
            moveTo(w * 0.18f, h * 0.38f)
            lineTo(w * 0.18f, h * 0.85f)
            lineTo(w * 0.82f, h * 0.85f)
            lineTo(w * 0.82f, h * 0.38f)
        }
        drawPath(walls, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Door
        val door = Path().apply {
            moveTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.40f, h * 0.55f)
            lineTo(w * 0.60f, h * 0.55f)
            lineTo(w * 0.60f, h * 0.85f)
        }
        drawPath(door, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconPhone(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = CharcoalText
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val receiver = Path().apply {
            moveTo(w * 0.24f, h * 0.18f)
            cubicTo(w * 0.12f, h * 0.28f, w * 0.24f, h * 0.52f, w * 0.43f, h * 0.69f)
            cubicTo(w * 0.61f, h * 0.86f, w * 0.78f, h * 0.91f, w * 0.84f, h * 0.77f)
        }
        drawPath(
            path = receiver,
            color = tint,
            style = Stroke(width = w * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
