package com.nasfinder.whattoeat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.nasfinder.whattoeat.data.LocationFailureReason
import com.nasfinder.whattoeat.data.MenuPolicy
import com.nasfinder.whattoeat.data.ImageLoader
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.model.RecommendationPhase
import com.nasfinder.whattoeat.model.Restaurant
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLineAlpha70
import com.nasfinder.whattoeat.theme.CaramelAlpha55
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.CharcoalSoft
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.SelectionMint
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.FoodImageView
import com.nasfinder.whattoeat.ui.components.IconChevronDown
import com.nasfinder.whattoeat.ui.components.IconChevronRight
import com.nasfinder.whattoeat.ui.components.IconDice5
import com.nasfinder.whattoeat.ui.components.IconForkKnife
import com.nasfinder.whattoeat.ui.components.IconCupSaucer
import com.nasfinder.whattoeat.ui.components.IconHeart
import com.nasfinder.whattoeat.ui.components.IconInformation
import com.nasfinder.whattoeat.ui.components.IconLeaf
import com.nasfinder.whattoeat.ui.components.IconPin
import com.nasfinder.whattoeat.ui.components.IconPhone
import com.nasfinder.whattoeat.ui.components.IconSearch
import com.nasfinder.whattoeat.ui.components.IconStar
import com.nasfinder.whattoeat.ui.components.IconStorefront
import com.nasfinder.whattoeat.ui.components.IconTakeout
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.PrimaryButton
import com.nasfinder.whattoeat.ui.components.SecondaryButton
import com.nasfinder.whattoeat.ui.components.openPhoneDialer
import com.nasfinder.whattoeat.viewmodel.MainViewModel
import kotlinx.coroutines.isActive
import androidx.compose.foundation.horizontalScroll
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.model.SituationFilter

@Composable
fun ResultScreen(viewModel: MainViewModel) {
    val phase by viewModel.recommendationPhase.collectAsState()
    val regionName by viewModel.currentRegionName.collectAsState()
    val main by viewModel.mainRestaurant.collectAsState()
    val carousel by viewModel.carouselRestaurants.collectAsState()
    val error by viewModel.recommendationError.collectAsState()
    val loadingSeconds by viewModel.loadingSeconds.collectAsState()
    val favoriteRecords by viewModel.favoriteRecords.collectAsState()
    val favoriteIds = favoriteRecords.map { it.restaurantId }.toSet()
    val selectedFilter by viewModel.selectedSituationFilter.collectAsState()
    val isCategoryFallbackApplied by viewModel.isCategoryFallbackApplied.collectAsState()
    val mapProvider by viewModel.selectedMapProvider.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CompactHeader(
            title = "오늘의 한 끼",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconDice5(tint = AccentRed, modifier = Modifier.size(18.dp)) } },
            onGearClick = { viewModel.openSettings() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_regionBar")
                .clickable(role = Role.Button) { viewModel.navigateTo(AppPage.REGION) }
                .padding(horizontal = 28.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = regionName.ifEmpty { "지역 다시 선택" }, style = AppTypography.rowTitle)
            IconChevronDown(tint = CharcoalText, modifier = Modifier.size(14.dp))
        }

        ResultSituationFilterBar(
            selectedFilter = selectedFilter,
            onSelectFilter = {
                viewModel.setSituationFilter(it)
                viewModel.retryRecommendation()
            },
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 2.dp)
        )

        if (isCategoryFallbackApplied && phase == RecommendationPhase.SUCCESS) {
            CategoryFallbackNotice(
                filter = selectedFilter,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp)
            )
        }

        when (phase) {
            RecommendationPhase.LOADING -> LoadingState(regionName, loadingSeconds, viewModel)
            RecommendationPhase.ERROR -> ErrorState(error ?: "문제가 생겼어요", viewModel)
            RecommendationPhase.LOCATION_DENIED -> LocationDeniedState(viewModel)
            RecommendationPhase.EMPTY -> EmptyState()
            RecommendationPhase.SUCCESS -> if (main != null) {
                ResultsState(
                    main = main!!,
                    carousel = carousel,
                    favoriteIds = favoriteIds,
                    regionName = regionName,
                    mapProvider = mapProvider,
                    viewModel = viewModel
                )
            }
            RecommendationPhase.IDLE -> {}
        }
    }
}
@Composable
internal fun MealShuffleAnimation(modifier: Modifier = Modifier, freezeForCatalog: Boolean = false) {
    var elapsedNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(freezeForCatalog) {
        if (freezeForCatalog) {
            elapsedNanos = 0L
            return@LaunchedEffect
        }
        var startedAt = 0L
        while (isActive) {
            withFrameNanos { frameTime ->
                if (startedAt == 0L) startedAt = frameTime
                elapsedNanos = frameTime - startedAt
            }
        }
    }

    val elapsedSeconds = elapsedNanos / 1_000_000_000f
    val orbitRotation = (elapsedSeconds / 2.4f * 360f) % 360f
    val diceRotation = (elapsedSeconds / 1.2f * 360f) % 360f
    val diceOffset = -5f + kotlin.math.sin(elapsedSeconds * (2f * Math.PI.toFloat() / 1.2f)) * 8f
    val isSelecting = (elapsedSeconds % 2.4f) >= 1.72f

    Box(
        modifier = modifier.size(width = 210.dp, height = 190.dp),
        contentAlignment = Alignment.Center
    ) {
        val radius = 65f
        val tokens = listOf<@Composable (Modifier) -> Unit>(
            { m -> IconForkKnife(tint = CharcoalText, modifier = m) },
            { m -> IconTakeout(tint = CharcoalText, modifier = m) },
            { m -> IconCupSaucer(tint = CharcoalText, modifier = m) },
            { m -> IconLeaf(tint = CharcoalText, modifier = m) }
        )

        tokens.forEachIndexed { index, tokenComposable ->
            val angleDeg = orbitRotation - 90f + index * 90f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val xOffset = (radius * kotlin.math.cos(angleRad)).dp
            val yOffset = (radius * kotlin.math.sin(angleRad)).dp

            ReferenceIconWell(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(34.dp),
                size = 34.dp
            ) {
                tokenComposable(Modifier.size(13.dp))
            }
        }

        val dieShape = RoundedCornerShape(17.dp)
        Box(
            modifier = Modifier
                .size(76.dp)
                .offset(y = diceOffset.dp)
                .rotate(diceRotation)
                .shadow(
                    elevation = 7.dp,
                    shape = dieShape,
                    ambientColor = CaramelDeep.copy(alpha = 0.18f),
                    spotColor = CaramelDeep.copy(alpha = 0.18f)
                )
                .background(
                    brush = Brush.linearGradient(listOf(Color.White, Ivory)),
                    shape = dieShape
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color.White, CanvasLineAlpha70, Color.White)),
                    shape = dieShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IconDice5(tint = CharcoalText, modifier = Modifier.size(42.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 9.dp)
                    .size(width = 30.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentRed)
                    .alpha(if (isSelecting) 1f else 0f)
            )
        }
    }
}

@Composable
private fun LoadingState(regionName: String, loadingSeconds: Int, viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MealShuffleAnimation(
            modifier = Modifier.testTag("result_loadingSpinner"),
            freezeForCatalog = viewModel.freezeMatchupLoadingAnimation
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${regionName.ifEmpty { "내 주변" }} 주변 오늘의 한 끼를 고르는 중…",
            style = AppTypography.body.copy(color = CharcoalSoft),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .testTag("result_loadingText")
        )
        Spacer(modifier = Modifier.height(20.dp))
        AnimatedVisibility(
            visible = loadingSeconds >= 10,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 4 }
        ) {
            SecondaryButton(
                text = "다시 고르기",
                onClick = { viewModel.retryRecommendation() },
                modifier = Modifier.testTag("result_retryButton")
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ReferenceIconWell(size = 64.dp) {
            IconInformation(tint = AccentRed, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "문제가 생겼어요", style = AppTypography.sectionTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = AppTypography.supporting,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("result_errorText")
        )
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = "지역 직접 선택",
            onClick = { viewModel.navigateTo(AppPage.REGION) },
            modifier = Modifier.testTag("result_specifyRegionButton")
        )
        Spacer(modifier = Modifier.height(10.dp))
        SecondaryButton(
            text = "다시 시도",
            onClick = { viewModel.retryRecommendation() },
            modifier = Modifier.testTag("result_retryButton")
        )
    }
}

@Composable
private fun LocationDeniedState(viewModel: MainViewModel) {
    val context = LocalContext.current
    val recoveryMessage by viewModel.locationRecoveryMessage.collectAsState()
    val failureReason by viewModel.locationFailureReason.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ReferenceIconWell(size = 64.dp) {
            IconPin(tint = AccentRed, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "위치를 찾을 수 없어요", style = AppTypography.sectionTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = recoveryMessage,
            style = AppTypography.supporting,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("result_locationDeniedText")
        )
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = "지역 직접 선택",
            onClick = { viewModel.navigateTo(AppPage.REGION) },
            modifier = Modifier.testTag("result_specifyRegionButton")
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (failureReason == LocationFailureReason.PERMISSION_DENIED || failureReason == LocationFailureReason.LOCATION_SERVICES_DISABLED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    text = "다시 시도",
                    onClick = { viewModel.retryRecommendation() },
                    modifier = Modifier.weight(1f).testTag("result_retryButton")
                )
                SecondaryButton(
                    text = "설정 열기",
                    onClick = { openAppSettings(context) },
                    modifier = Modifier.weight(1f).testTag("result_settingsButton")
                )
            }
        } else {
            SecondaryButton(
                text = "다시 시도",
                onClick = { viewModel.retryRecommendation() },
                modifier = Modifier.testTag("result_retryButton")
            )
        }
    }
}

private fun openAppSettings(context: android.content.Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // ignore
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ReferenceIconWell(size = 64.dp) {
            IconForkKnife(tint = AccentRed, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "주변 음식점을 찾지 못했어요", style = AppTypography.sectionTitle)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "지역을 바꾸거나 다시 골라 주세요.", style = AppTypography.supporting)
    }
}

@Composable
private fun ResultsState(
    main: Restaurant,
    carousel: List<Restaurant>,
    favoriteIds: Set<String>,
    regionName: String,
    mapProvider: MapProvider,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val restaurants = listOf(main) + carousel
    val fallbackTypes = remember(restaurants) {
        val used = mutableSetOf<ImageLoader.FallbackType>()
        restaurants.associate { restaurant ->
            val type = ImageLoader.resolveFallbackType(
                category = restaurant.category,
                menu = MenuPolicy.resolveMenu(restaurant),
                seed = restaurant.id,
                excluding = used
            )
            used += type
            restaurant.id to type
        }
    }
    val menu = MenuPolicy.resolveMenu(main)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        MainResultCard(main, favoriteIds.contains(main.id), fallbackTypes[main.id], viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        MapSearchActionCard(
            menu = menu,
            regionName = regionName,
            mapProvider = mapProvider,
            onSearchMap = {
                viewModel.searchMapForMenu(context, menu, regionName)
            }
        )

        if (carousel.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "함께 보면 좋은 맛집",
                style = AppTypography.headline
            )
            Spacer(modifier = Modifier.height(5.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_secondaryGrid"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                carousel.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { restaurant ->
                            SecondaryGridCard(
                                restaurant = restaurant,
                                isFavorite = favoriteIds.contains(restaurant.id),
                                fallbackType = fallbackTypes[restaurant.id],
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun MainResultCard(
    main: Restaurant,
    isFavorite: Boolean,
    fallbackType: ImageLoader.FallbackType?,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val menu = MenuPolicy.resolveMenu(main)
    val categoryLeaf = main.category.substringAfterLast('>').trim().ifEmpty { menu }
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .testTag("result_mainCard")
            .shadow(elevation = 4.dp, shape = shape, ambientColor = CaramelDeep.copy(alpha = 0.16f), spotColor = CaramelDeep.copy(alpha = 0.16f))
            .background(Ivory, shape)
            .border(1.dp, CaramelAlpha55, shape)
            .clip(shape)
            .clickable(role = Role.Button) { viewModel.openDecision(main) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            FoodImageView(
                photoUrl = main.photoUrl,
                category = main.category,
                menu = menu,
                seed = main.id,
                fallbackType = fallbackType,
                photoInformation = main.photoInformation,
                onInfoClick = main.photoInformation.takeIf { it.hasDetails }?.let { { viewModel.showPhotoInformation(it) } },
                modifier = Modifier.width(158.dp).fillMaxHeight(),
                cornerRadius = 0.dp
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 14.dp, top = 16.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(SelectionMint, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = menu, style = AppTypography.caption2)
                }
                Text(text = main.name, style = AppTypography.title2, maxLines = 2)
                Text(text = categoryLeaf, style = AppTypography.subheadline, maxLines = 1)
                main.distanceMeters?.let { d ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconPin(tint = AccentRed, modifier = Modifier.size(14.dp))
                        Text(text = "약 ${d}m", style = AppTypography.caption)
                    }
                }
                Text(text = "가까워서 더 반가운 한 끼", style = AppTypography.caption2)
                Spacer(modifier = Modifier.weight(1f).height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    main.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { phone ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Ivory, CircleShape)
                                .border(1.dp, CanvasLineAlpha70, CircleShape)
                                .clip(CircleShape)
                                .semantics { contentDescription = "${main.name}에 전화걸기" }
                                .clickable(role = Role.Button) { openPhoneDialer(context, phone) },
                            contentAlignment = Alignment.Center
                        ) {
                            IconPhone(tint = CharcoalText, modifier = Modifier.size(15.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("result_viewButton")
                            .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = androidx.compose.ui.graphics.Color(0x18000000), spotColor = androidx.compose.ui.graphics.Color(0x18000000))
                            .background(Ivory, CircleShape)
                            .border(1.dp, CanvasLineAlpha70, CircleShape)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { viewModel.openDecision(main) }
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "이곳 보기",
                            style = AppTypography.caption.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            ),
                            maxLines = 1
                        )
                        IconChevronRight(tint = CharcoalText, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 14.dp)
                .size(40.dp)
                .background(Ivory.copy(alpha = 0.94f), CircleShape)
                .border(1.dp, CanvasLineAlpha70, CircleShape)
                .clip(CircleShape)
                .clickable(role = Role.Button) { viewModel.toggleFavorite(main) }
                .testTag("result_mainHeart"),
            contentAlignment = Alignment.Center
        ) {
            IconHeart(
                tint = if (isFavorite) AccentRed else CharcoalText,
                isFilled = isFavorite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SecondaryGridCard(
    restaurant: Restaurant,
    isFavorite: Boolean,
    fallbackType: ImageLoader.FallbackType?,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val menu = MenuPolicy.resolveMenu(restaurant)
    val shortCategory = restaurant.category.substringAfterLast('>').trim()
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .testTag("result_secondaryCard_${restaurant.id}")
            .background(Ivory, shape)
            .border(1.dp, CanvasLineAlpha70, shape)
            .clip(shape)
            .clickable(role = Role.Button) { viewModel.openDecision(restaurant) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
            FoodImageView(
                photoUrl = restaurant.photoUrl,
                category = restaurant.category,
                menu = menu,
                seed = restaurant.id,
                fallbackType = fallbackType,
                photoInformation = restaurant.photoInformation,
                onInfoClick = restaurant.photoInformation.takeIf { it.hasDetails }
                    ?.let { { viewModel.showPhotoInformation(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp),
                cornerRadius = 0.dp
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(40.dp)
                    .background(Ivory.copy(alpha = 0.92f), CircleShape)
                    .border(1.dp, CanvasLineAlpha70, CircleShape)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { viewModel.toggleFavorite(restaurant) },
                contentAlignment = Alignment.Center
            ) {
                IconHeart(
                    tint = if (isFavorite) AccentRed else CharcoalText,
                    isFilled = isFavorite,
                    modifier = Modifier.size(16.dp)
                )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 9.dp,
                        end = if (restaurant.phone.isNullOrBlank()) 9.dp else 49.dp,
                        top = 7.dp,
                        bottom = 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = restaurant.name,
                    style = AppTypography.smallCardName,
                    maxLines = 1
                )
                Text(
                    text = shortCategory,
                    style = AppTypography.smallCardMeta,
                    maxLines = 1
                )
                restaurant.distanceMeters?.let {
                    Text(
                        text = "약 ${it}m",
                        style = AppTypography.smallCardMeta,
                        maxLines = 1
                    )
                }
            }
        }
        restaurant.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { phone ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .size(36.dp)
                    .background(Ivory, CircleShape)
                    .border(1.dp, CanvasLineAlpha70, CircleShape)
                    .clip(CircleShape)
                    .semantics { contentDescription = "${restaurant.name}에 전화걸기" }
                    .clickable(role = Role.Button) { openPhoneDialer(context, phone) },
                contentAlignment = Alignment.Center
            ) {
                IconPhone(tint = CharcoalText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ResultSituationFilterBar(
    selectedFilter: SituationFilter,
    onSelectFilter: (SituationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .testTag("result_situationFilterRow"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SituationFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .testTag("result_situationFilter_${filter.name.lowercase()}")
                    .shadow(
                        elevation = if (isSelected) 2.dp else 0.dp,
                        shape = shape,
                        ambientColor = CaramelDeep.copy(alpha = 0.08f),
                        spotColor = CaramelDeep.copy(alpha = 0.08f)
                    )
                    .background(if (isSelected) SelectionMint else Ivory, shape)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) AccentRed.copy(alpha = 0.65f) else CanvasLineAlpha70,
                        shape = shape
                    )
                    .clip(shape)
                    .clickable(role = Role.Button) { onSelectFilter(filter) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.displayName,
                    style = AppTypography.caption.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AccentRed else CharcoalText
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryFallbackNotice(
    filter: SituationFilter,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_categoryFallbackNotice")
            .background(SelectionMint.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .border(1.dp, CanvasLineAlpha70, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReferenceIconWell(size = 24.dp) {
            IconInformation(tint = AccentRed, modifier = Modifier.size(12.dp))
        }
        Text(
            text = "선택한 ‘${filter.displayName}’ 조건에 맞는 식당이 없어 전체 메뉴에서 추천했어요.",
            style = AppTypography.caption2.copy(color = CharcoalText)
        )
    }
}

@Composable
private fun MapSearchActionCard(
    menu: String,
    regionName: String,
    mapProvider: MapProvider,
    onSearchMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val queryText = if (regionName.isNotEmpty() && regionName != "현 위치" && regionName != "지정 지역") {
        "$regionName ‘$menu’"
    } else {
        "‘$menu’"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("result_mapSearchCta")
            .shadow(elevation = 2.dp, shape = shape, ambientColor = CaramelDeep.copy(alpha = 0.08f), spotColor = CaramelDeep.copy(alpha = 0.08f))
            .background(Ivory, shape)
            .border(1.dp, CanvasLineAlpha70, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onSearchMap)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReferenceIconWell(size = 32.dp) {
            IconSearch(tint = AccentRed, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${mapProvider.shortName} 지도에서 $queryText 맛집 검색",
                style = AppTypography.caption.copy(fontWeight = FontWeight.SemiBold, color = CharcoalText),
                maxLines = 1
            )
            Text(
                text = "${mapProvider.displayName} 지도 앱으로 이동하여 주변 검색 결과를 확인해요.",
                style = AppTypography.caption2.copy(color = CharcoalSoft),
                maxLines = 1
            )
        }
        IconChevronRight(tint = CharcoalText, modifier = Modifier.size(12.dp))
    }
}
