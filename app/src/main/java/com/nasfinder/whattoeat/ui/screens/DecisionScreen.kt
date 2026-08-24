package com.nasfinder.whattoeat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLine
import com.nasfinder.whattoeat.theme.Caramel
import com.nasfinder.whattoeat.theme.CaramelAlpha55
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.SelectionMint
import com.nasfinder.whattoeat.data.ImageLoader
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.FoodImageView
import com.nasfinder.whattoeat.ui.components.IconCheckSeal
import com.nasfinder.whattoeat.ui.components.IconChevronRight
import com.nasfinder.whattoeat.ui.components.IconInformation
import com.nasfinder.whattoeat.ui.components.IconMap
import com.nasfinder.whattoeat.ui.components.IconNavigationArrow
import com.nasfinder.whattoeat.ui.components.IconPin
import com.nasfinder.whattoeat.ui.components.IconPhone
import com.nasfinder.whattoeat.ui.components.IconQuestionClock
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.openPhoneDialer
import com.nasfinder.whattoeat.viewmodel.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

@Composable
fun DecisionScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val decision by viewModel.currentDecision.collectAsState()
    val isRecorded by viewModel.isCurrentDecisionRecorded.collectAsState()
    val mapProvider by viewModel.selectedMapProvider.collectAsState()

    val current = decision
    if (current == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.closeDecision() }
        return
    }
    val restaurant = current.restaurant
    val categoryLabel = restaurant.category.substringAfterLast(" > ")

    Column(modifier = Modifier.fillMaxSize()) {
        CompactHeader(
            title = "오늘의 결정",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconCheckSeal(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(18.dp)) } },
            onCloseClick = { viewModel.closeDecision() }
        )

        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Hero
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .testTag("decision_hero")
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = CaramelDeep.copy(alpha = 0.12f), spotColor = CaramelDeep.copy(alpha = 0.12f))
                    .background(Ivory, RoundedCornerShape(20.dp))
                    .border(1.dp, Caramel.copy(alpha = 0.48f), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp)),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                FoodImageView(
                    photoUrl = restaurant.photoUrl,
                    category = restaurant.category,
                    menu = current.menu,
                    seed = restaurant.id,
                    photoInformation = restaurant.photoInformation,
                    onInfoClick = restaurant.photoInformation.takeIf { it.hasDetails }?.let { { viewModel.showPhotoInformation(it) } },
                    modifier = Modifier.width(132.dp).fillMaxHeight(),
                    cornerRadius = 0.dp
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SelectionMint.copy(alpha = 0.72f), Ivory)
                            )
                        )
                        .padding(vertical = 14.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (categoryLabel != current.menu) {
                        Box(
                            modifier = Modifier
                                .background(SelectionMint, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = categoryLabel, style = AppTypography.caption.copy(color = com.nasfinder.whattoeat.theme.MintInk))
                        }
                    }
                    Text(text = current.menu, style = AppTypography.decisionMenu)
                    Text(text = restaurant.name, style = AppTypography.decisionRestaurant)
                    restaurant.distanceMeters?.let { d ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconNavigationArrow(tint = AccentRed, modifier = Modifier.size(12.dp))
                            Text(text = "여기서 약 ${d}m", style = AppTypography.caption.copy(color = AccentRed))
                        }
                    }
                }
            }

            restaurant.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { phone ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Ivory, RoundedCornerShape(16.dp))
                        .border(1.dp, CanvasLine, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .semantics { contentDescription = "${restaurant.name} ${phone}에 전화걸기" }
                        .clickable(role = Role.Button) {
                            openPhoneDialer(context, phone)
                        }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReferenceIconWell(size = 34.dp) {
                        IconPhone(tint = AccentRed, modifier = Modifier.size(14.dp))
                    }
                    Text(text = phone, style = AppTypography.subheadline.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), maxLines = 1)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    Text(text = "전화걸기", style = AppTypography.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.nasfinder.whattoeat.theme.MintInk), maxLines = 1)
                    IconChevronRight(tint = com.nasfinder.whattoeat.theme.CharcoalSoft, modifier = Modifier.size(12.dp))
                }
            }

            // Map preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(244.dp)
                    .testTag("decision_mapCard")
                    .background(Ivory, RoundedCornerShape(20.dp))
                    .border(1.dp, CanvasLine, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button) {
                        viewModel.openMapForRestaurant(context, restaurant)
                    }
            ) {
                MapPreview(
                    latitude = restaurant.lat,
                    longitude = restaurant.lng,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Ivory.copy(alpha = 0.95f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconMap(tint = CharcoalText, modifier = Modifier.size(14.dp))
                    Text(text = "${mapProvider.shortName}로 길 찾기", style = AppTypography.providerLabel)
                }

                val address = restaurant.roadAddress ?: restaurant.address
                if (!address.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Ivory.copy(alpha = 0.95f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconPin(tint = AccentRed, modifier = Modifier.size(12.dp))
                        Text(text = address, style = AppTypography.caption2)
                    }
                }
            }

            // CTA
            if (!isRecorded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("decision_cta")
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                        .background(Ivory, RoundedCornerShape(16.dp))
                        .border(1.dp, CaramelAlpha55, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) { viewModel.recordCurrentDecision(context) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReferenceIconWell(size = 34.dp) { IconCheckSeal(tint = AccentRed, modifier = Modifier.size(18.dp)) }
                    Text(
                        text = "오늘은 여기로",
                        style = AppTypography.headline,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    IconChevronRight(tint = CharcoalText, modifier = Modifier.size(16.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("decision_recordedRow")
                        .background(SelectionMint, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) { viewModel.openMapForRestaurant(context, restaurant) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReferenceIconWell(size = 34.dp) { IconCheckSeal(tint = AccentRed, modifier = Modifier.size(18.dp)) }
                    Text(text = "최근 한 끼에 담았어요", style = AppTypography.rowTitle.copy(color = com.nasfinder.whattoeat.theme.MintInk))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decision_businessInfo")
                    .clickable(role = Role.Button) { viewModel.showBusinessInfo() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconQuestionClock(tint = AccentRed, modifier = Modifier.size(16.dp))
                Text(
                    text = "영업 정보는 지도에서 확인",
                    style = AppTypography.supporting,
                    modifier = Modifier.weight(1f)
                )
                IconInformation(tint = CharcoalText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun MapPreview(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    val zoom = 16
    val scale = 2.0.pow(zoom)
    val x = (longitude + 180.0) / 360.0 * scale
    val latitudeRadians = latitude * PI / 180.0
    val y = (1.0 - ln(tan(latitudeRadians) + 1.0 / cos(latitudeRadians)) / PI) / 2.0 * scale
    val tileX = floor(x).toInt()
    val tileY = floor(y).toInt()
    val fractionX = x - floor(x)
    val fractionY = y - floor(y)
    val tiles = remember(latitude, longitude) { mutableStateMapOf<Pair<Int, Int>, androidx.compose.ui.graphics.ImageBitmap>() }

    LaunchedEffect(latitude, longitude) {
        coroutineScope {
            (-1..1).flatMap { dy ->
                (-1..1).map { dx ->
                    async {
                        val key = dx to dy
                        key to ImageLoader.loadImage("https://tile.openstreetmap.org/$zoom/${tileX + dx}/${tileY + dy}.png")?.asImageBitmap()
                    }
                }
            }.awaitAll().forEach { (key, bitmap) -> bitmap?.let { tiles[key] = it } }
        }
    }

    BoxWithConstraints(modifier = modifier.background(androidx.compose.ui.graphics.Color(0xFFE7EFE2))) {
        val tileSize = 256.dp
        for (dy in -1..1) for (dx in -1..1) {
            tiles[dx to dy]?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset(
                            x = maxWidth / 2 - tileSize * fractionX.toFloat() + tileSize * dx,
                            y = maxHeight / 2 - tileSize * fractionY.toFloat() + tileSize * dy
                        )
                        .size(tileSize)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp)
                .shadow(5.dp, CircleShape)
                .background(AccentRed, CircleShape)
                .border(4.dp, androidx.compose.ui.graphics.Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(10.dp).background(androidx.compose.ui.graphics.Color.White, CircleShape))
        }
        Text(
            text = "© OpenStreetMap",
            style = AppTypography.caption2,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp)
                .background(Ivory.copy(alpha = 0.88f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
