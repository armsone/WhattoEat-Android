package com.nasfinder.whattoeat.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.DashPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.R
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.Caramel
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.LeatherLight

@Composable
fun AppBottomBar(
    currentPage: AppPage,
    onTabSelected: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources
    val leatherTexture = remember(resources) {
        ImageBitmap.imageResource(resources, R.drawable.img_leather_texture)
    }
    val barShape = remember { RaisedCenterBarShape() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(bottom = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .height(126.dp)
                .testTag("bottom_bar")
                .shadow(
                    elevation = 6.dp,
                    shape = barShape,
                    ambientColor = CaramelDeep.copy(alpha = 0.22f),
                    spotColor = CaramelDeep.copy(alpha = 0.22f)
                )
                .background(color = Caramel, shape = barShape)
        ) {
            LeatherBarDetail(texture = leatherTexture, modifier = Modifier.fillMaxSize())
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 21.dp, end = 12.dp, bottom = 15.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BottomTabItem(
                    label = "홈",
                    icon = { tint -> IconHome(tint = tint, modifier = Modifier.size(30.dp)) },
                    isSelected = currentPage == AppPage.HOME,
                    onClick = { onTabSelected(AppPage.HOME) }
                )

                BottomTabItem(
                    label = "지역",
                    icon = { tint -> IconMap(tint = tint, modifier = Modifier.size(30.dp)) },
                    isSelected = currentPage == AppPage.REGION,
                    onClick = { onTabSelected(AppPage.REGION) }
                )

                CenterRecommendTabItem(
                    label = "추천",
                    isSelected = currentPage == AppPage.RESULT,
                    accessibilityLabel = "추천 다시 고르기",
                    onClick = { onTabSelected(AppPage.RESULT) }
                )

                BottomTabItem(
                    label = "최근",
                    icon = { tint -> IconHistory(tint = tint, modifier = Modifier.size(30.dp)) },
                    isSelected = currentPage == AppPage.HISTORY,
                    onClick = { onTabSelected(AppPage.HISTORY) }
                )

                BottomTabItem(
                    label = "찜",
                    icon = { tint -> IconHeart(tint = tint, modifier = Modifier.size(30.dp), isFilled = false) },
                    isSelected = currentPage == AppPage.FAVORITES,
                    onClick = { onTabSelected(AppPage.FAVORITES) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomTabItem(
    label: String,
    icon: @Composable (Color) -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    accessibilityLabel: String? = null
) {
    val tint = Ivory.copy(alpha = if (isSelected) 1f else 0.7f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .semantics {
                this.role = Role.Tab
                this.selected = isSelected
                if (accessibilityLabel != null) {
                    this.contentDescription = accessibilityLabel
                }
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon(tint)
            Text(
                text = label,
                style = AppTypography.bottomLabel.copy(
                    color = tint,
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .size(width = 21.dp, height = 3.75.dp)
                    .background(AccentRed, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun RowScope.CenterRecommendTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accessibilityLabel: String? = null
) {
    val tint = Ivory.copy(alpha = if (isSelected) 1f else 0.7f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .semantics {
                this.role = Role.Tab
                this.selected = isSelected
                if (accessibilityLabel != null) {
                    this.contentDescription = accessibilityLabel
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(30.dp),
                contentAlignment = Alignment.Center
            ) {
                RecommendCircleIcon(
                    isSelected = isSelected,
                    modifier = Modifier.offset(y = (-15.897).dp)
                )
            }
            Text(
                text = label,
                style = AppTypography.bottomLabel.copy(
                    color = tint,
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .size(width = 21.dp, height = 3.75.dp)
                    .background(AccentRed, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun RecommendCircleIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val circleSize = 57.834.dp
    val strokeWidth = 1.8.dp
    val shadowRadius = 7.5.dp
    val shadowOffsetY = 4.5.dp
    val diceSize = 36.dp

    Canvas(modifier = modifier.requiredSize(circleSize).testTag("bottom_recommendCircle")) {
        val w = size.width
        val h = size.height
        val radius = w / 2f
        val strokePx = strokeWidth.toPx()
        val opticalLift = 3.dp.toPx()
        val center = Offset(radius, radius - opticalLift)

        drawIntoCanvas { canvas ->
            val shadowPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    shadowRadius.toPx(),
                    0f,
                    shadowOffsetY.toPx(),
                    (if (isSelected) AccentRed.copy(alpha = 0.28f) else CaramelDeep.copy(alpha = 0.2f)).toArgb()
                )
            }
            canvas.nativeCanvas.drawCircle(center.x, center.y, radius - strokePx / 2f, shadowPaint)
        }

        val gradientBrush = if (isSelected) {
            Brush.linearGradient(
                colors = listOf(Color(0xFFF53338), AccentRed),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        } else {
            Brush.verticalGradient(colors = listOf(Color.White, Ivory))
        }
        drawCircle(
            brush = gradientBrush,
            radius = radius - strokePx / 2f,
            center = center
        )

        drawCircle(
            color = if (isSelected) Color.White.copy(alpha = 0.88f) else CaramelDeep.copy(alpha = 0.35f),
            radius = radius - strokePx / 2f,
            center = center,
            style = Stroke(width = strokePx)
        )

        val dicePx = diceSize.toPx()
        val diceLeft = (w - dicePx) / 2f
        val diceTop = (h - dicePx) / 2f - opticalLift
        val cornerRad = dicePx * 0.22f

        drawRoundRect(
            color = if (isSelected) Color.White else AccentRed,
            topLeft = Offset(diceLeft + dicePx * 0.08f, diceTop + dicePx * 0.08f),
            size = Size(dicePx * 0.84f, dicePx * 0.84f),
            cornerRadius = CornerRadius(cornerRad, cornerRad)
        )

        val dotRadius = dicePx * 0.07f
        val dotColor = if (isSelected) AccentRed else Color.White
        val dcx = diceLeft + dicePx * 0.5f
        val dcy = diceTop + dicePx * 0.5f
        val dOffset = dicePx * 0.20f

        drawCircle(dotColor, radius = dotRadius, center = Offset(dcx, dcy))
        drawCircle(dotColor, radius = dotRadius, center = Offset(dcx - dOffset, dcy - dOffset))
        drawCircle(dotColor, radius = dotRadius, center = Offset(dcx + dOffset, dcy - dOffset))
        drawCircle(dotColor, radius = dotRadius, center = Offset(dcx - dOffset, dcy + dOffset))
        drawCircle(dotColor, radius = dotRadius, center = Offset(dcx + dOffset, dcy + dOffset))
    }
}

@Composable
private fun LeatherBarDetail(texture: ImageBitmap, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val full = RaisedCenterBarShape.createPath(size, this, 0.dp)
        val inner = RaisedCenterBarShape.createPath(size, this, 3.dp)
        val stitch = RaisedCenterBarShape.createPath(size, this, 7.5.dp)
        clipPath(full) {
            val tileWidth = 96.dp.roundToPx()
            val tileHeight = 60.dp.roundToPx()
            var y = 0
            while (y < size.height.toInt()) {
                var x = 0
                while (x < size.width.toInt()) {
                    drawImage(
                        image = texture,
                        dstOffset = IntOffset(x, y),
                        dstSize = IntSize(tileWidth, tileHeight)
                    )
                    x += tileWidth
                }
                y += tileHeight
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Ivory.copy(alpha = 0.12f),
                        Color.Transparent,
                        CaramelDeep.copy(alpha = 0.16f)
                    )
                )
            )
        }
        drawPath(
            path = full,
            color = CaramelDeep.copy(alpha = 0.8f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawPath(
            path = inner,
            color = LeatherLight.copy(alpha = 0.58f),
            style = Stroke(width = 1.2.dp.toPx())
        )
        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
        drawIntoCanvas { canvas ->
            val shadowPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.75.dp.toPx()
                strokeCap = android.graphics.Paint.Cap.ROUND
                color = CaramelDeep.copy(alpha = 0.68f).toArgb()
                pathEffect = DashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)
                maskFilter = BlurMaskFilter(0.45.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.save()
            canvas.nativeCanvas.translate(0f, 0.825.dp.toPx())
            canvas.nativeCanvas.drawPath(stitch.asAndroidPath(), shadowPaint)
            canvas.nativeCanvas.restore()
        }
        drawPath(
            path = stitch,
            color = Ivory.copy(alpha = 0.72f),
            style = Stroke(width = 1.725.dp.toPx(), pathEffect = dash)
        )
    }
}

private class RaisedCenterBarShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(createPath(size, density, 0.dp))
    }

    companion object {
        fun createPath(size: Size, density: Density, inset: Dp): Path {
            val insetPx = with(density) { inset.toPx() }
            val topEdgePx = with(density) { 30.dp.toPx() } + insetPx
            val shoulderPx = (with(density) { 66.dp.toPx() } - insetPx).coerceAtLeast(with(density) { 12.dp.toPx() })
            val cornerRadiusPx = (with(density) { 33.dp.toPx() } - insetPx).coerceAtLeast(with(density) { 6.dp.toPx() })
            val control32 = with(density) { 48.dp.toPx() }
            val control30 = with(density) { 45.dp.toPx() }
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val left = insetPx
            val right = w - insetPx
            val bottom = h - insetPx

            return Path().apply {
            moveTo(left, topEdgePx + cornerRadiusPx)
            arcTo(
                rect = Rect(left, topEdgePx, left + cornerRadiusPx * 2f, topEdgePx + cornerRadiusPx * 2f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cx - shoulderPx, topEdgePx)
            cubicTo(
                cx - control32, topEdgePx,
                cx - control30, insetPx,
                cx, insetPx
            )
            cubicTo(
                cx + control30, insetPx,
                cx + control32, topEdgePx,
                cx + shoulderPx, topEdgePx
            )
            lineTo(right - cornerRadiusPx, topEdgePx)
            arcTo(
                rect = Rect(right - cornerRadiusPx * 2f, topEdgePx, right, topEdgePx + cornerRadiusPx * 2f),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right, bottom - cornerRadiusPx)
            arcTo(
                rect = Rect(right - cornerRadiusPx * 2f, bottom - cornerRadiusPx * 2f, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left + cornerRadiusPx, bottom)
            arcTo(
                rect = Rect(left, bottom - cornerRadiusPx * 2f, left + cornerRadiusPx * 2f, bottom),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left, topEdgePx + cornerRadiusPx)
            close()
            }
        }
    }
}
