package com.nasfinder.whattoeat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nasfinder.whattoeat.R
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.Chrome
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory

@Composable
fun ReferenceIconWell(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color(0x242E3338),
                spotColor = Color(0x242E3338)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Ivory)
                ),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Chrome.copy(alpha = 0.75f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun RedWell(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color(0x242E3338),
                spotColor = Color(0x242E3338)
            )
            .background(
                color = AccentRed,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun WordmarkView(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_wordmark),
            contentDescription = "오늘 뭐 먹지",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 162.dp, height = 41.dp)
        )
        Text(
            text = "??",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                letterSpacing = (-1.5).sp,
                color = CharcoalText
            ),
            modifier = Modifier.offset(x = (-12).dp, y = 1.dp)
        )
    }
}

@Composable
fun PinWellView(
    modifier: Modifier = Modifier.size(58.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.img_pin_well),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun LunchHeroView(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.img_lunch_hero),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
