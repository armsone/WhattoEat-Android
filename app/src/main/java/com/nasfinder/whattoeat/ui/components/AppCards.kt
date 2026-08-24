package com.nasfinder.whattoeat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLineAlpha75
import com.nasfinder.whattoeat.theme.CanvasLineAlpha80
import com.nasfinder.whattoeat.theme.Caramel
import com.nasfinder.whattoeat.theme.CaramelAlpha55
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Chrome
import com.nasfinder.whattoeat.theme.Ivory
import kotlin.math.roundToInt

@Composable
fun ReferenceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = Ivory,
    borderColor: Color = CanvasLineAlpha80,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .shadow(
            elevation = 3.dp,
            shape = shape,
            ambientColor = CaramelDeep.copy(alpha = 0.08f),
            spotColor = CaramelDeep.copy(alpha = 0.08f)
        )
        .background(color = backgroundColor, shape = shape)
        .border(width = 1.dp, color = borderColor, shape = shape)
        .then(
            if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier
        )
        .padding(16.dp)

    Column(
        modifier = cardModifier,
        content = content
    )
}

@Composable
fun SwipeRevealRow(
    onAction: () -> Unit,
    actionContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityActionLabel: String = "삭제",
    content: @Composable () -> Unit
) {
    val actionWidth = 76.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val outerShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(accessibilityActionLabel) {
                        onAction()
                        true
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidth)
                .height(76.dp)
                .background(AccentRed)
                .clickable(role = Role.Button, onClick = onAction),
            contentAlignment = Alignment.Center
        ) {
            actionContent()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(Ivory)
                .border(width = 1.dp, color = CanvasLineAlpha75, shape = outerShape)
                .pointerInput(actionWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(-actionWidthPx, 0f)
                        },
                        onDragEnd = {
                            offsetX = if (offsetX < -actionWidthPx * 0.4f) -actionWidthPx else 0f
                        },
                        onDragCancel = { offsetX = 0f }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun CompactHeader(
    title: String,
    leftWell: @Composable () -> Unit,
    onGearClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            leftWell()
            Text(
                text = title,
                style = AppTypography.screenTitle
            )
        }

        if (onGearClick != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onGearClick,
                        role = Role.Button
                    )
                    .semantics { contentDescription = "설정" },
                contentAlignment = Alignment.Center
            ) {
                ReferenceIconWell(size = 34.dp) {
                    IconGear(
                        modifier = Modifier.size(18.dp),
                        tint = AccentRed
                    )
                }
            }
        } else if (onCloseClick != null) {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 54.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
                    .background(color = Ivory, shape = CircleShape)
                    .border(width = 1.dp, color = Chrome.copy(alpha = 0.8f), shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onCloseClick, role = Role.Button)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "닫기",
                    style = AppTypography.rowTitle
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 2.dp, shape = shape, ambientColor = CaramelDeep.copy(alpha = 0.15f), spotColor = CaramelDeep.copy(alpha = 0.15f))
            .background(color = Caramel, shape = shape)
            .border(width = 1.dp, color = Chrome.copy(alpha = 0.9f), shape = shape)
            .clip(shape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.headline.copy(color = Ivory)
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 1.dp, shape = shape, ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
            .background(color = Ivory, shape = shape)
            .border(width = 1.dp, color = Chrome, shape = shape)
            .clip(shape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.headline.copy(color = CharcoalText)
        )
    }
}
