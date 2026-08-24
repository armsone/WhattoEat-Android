package com.nasfinder.whattoeat.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.R
import com.nasfinder.whattoeat.data.ImageLoader
import com.nasfinder.whattoeat.model.PhotoInformation
import com.nasfinder.whattoeat.theme.CanvasLine
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory

@Composable
fun FoodImageView(
    photoUrl: String?,
    category: String,
    menu: String,
    seed: String,
    fallbackType: ImageLoader.FallbackType? = null,
    photoInformation: PhotoInformation?,
    onInfoClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    showBadges: Boolean = true
) {
    val shape = RoundedCornerShape(cornerRadius)
    var remoteBitmap by remember(photoUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoInformation?.provider, photoUrl) {
        remoteBitmap = null
        if (!photoUrl.isNullOrEmpty()) {
            remoteBitmap = ImageLoader.loadImage(photoUrl, photoInformation?.provider)
        }
    }

    val resolvedFallbackType = remember(category, menu, seed, fallbackType) {
        fallbackType ?: ImageLoader.resolveFallbackType(category, menu, seed)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFF3EDE2))
            .semantics {
                contentDescription = if (remoteBitmap == null) {
                    "${category.substringAfterLast('>').trim().ifEmpty { "음식점" }} 음식 예시 이미지"
                } else {
                    when (photoInformation?.kind) {
                        "restaurantVerified" -> "해당 식당 사진"
                        "categoryExample" -> "메뉴 예시 사진, 해당 식당 사진이 아님"
                        else -> "참고 이미지"
                    }
                }
            }
    ) {
        // 1. Fallback rendering: exact source bitmap (FoodMain/FoodSide1-3), scaledToFill+clip per spec 4.4
        Image(
            painter = painterResource(id = resolvedFallbackType.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Remote photo overlay if loaded
        AnimatedVisibility(
            visible = remoteBitmap != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier.fillMaxSize()
        ) {
            remoteBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. Badges overlay
        if (showBadges) {
            if (remoteBitmap != null && photoInformation?.hasDetails == true && onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(26.dp)
                        .background(
                            color = Ivory.copy(alpha = 0.94f),
                            shape = CircleShape
                        )
                        .border(
                            width = 0.75.dp,
                            color = CanvasLine.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = onInfoClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconInformation(tint = CharcoalText, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyRecentIllustration(
    modifier: Modifier = Modifier.size(width = 210.dp, height = 150.dp)
) {
    // Exact source bitmap: EmptyRecent.png 768x768, 210x150 scaledToFill/radius24 per spec 4.4
    Image(
        painter = painterResource(id = R.drawable.img_empty_recent),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    )
}

@Composable
fun EmptyFavoritesIllustration(
    modifier: Modifier = Modifier.size(width = 210.dp, height = 150.dp)
) {
    // Exact source bitmap: EmptyFavorites.png 1254x1254, 210x150 scaledToFill/radius24 per spec 4.4
    Image(
        painter = painterResource(id = R.drawable.img_empty_favorites),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    )
}
