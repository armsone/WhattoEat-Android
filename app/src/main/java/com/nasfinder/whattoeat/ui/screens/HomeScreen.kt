package com.nasfinder.whattoeat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nasfinder.whattoeat.model.LocationMode
import com.nasfinder.whattoeat.model.AppPage
import com.nasfinder.whattoeat.model.SituationFilter
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLineAlpha80
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.SelectionMint
import com.nasfinder.whattoeat.ui.components.IconChevronRight
import com.nasfinder.whattoeat.ui.components.IconGear
import com.nasfinder.whattoeat.ui.components.IconNavigationArrow
import com.nasfinder.whattoeat.ui.components.IconSearch
import com.nasfinder.whattoeat.ui.components.LunchHeroView
import com.nasfinder.whattoeat.ui.components.PinWellView
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.WordmarkView
import com.nasfinder.whattoeat.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val selectedFilter by viewModel.selectedSituationFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 14.dp, bottom = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WordmarkView()
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = { viewModel.openSettings() })
                    .testTag("home_settings"),
                contentAlignment = Alignment.Center
            ) {
                ReferenceIconWell(size = 34.dp) {
                    IconGear(tint = AccentRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HomeHeroCta(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        SituationFilterRow(
            selectedFilter = selectedFilter,
            onSelectFilter = { viewModel.setSituationFilter(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PinWellView()
            Text(text = "어디서 드실까요?", style = AppTypography.title2)
        }

        Spacer(modifier = Modifier.height(20.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stitchRailInset = maxWidth * 0.025f
            Column(
                modifier = Modifier.padding(horizontal = stitchRailInset),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HomeLocationCard(
                    icon = {
                        ReferenceIconWell(size = 50.dp) {
                            IconNavigationArrow(tint = com.nasfinder.whattoeat.theme.MintInk, modifier = Modifier.size(24.dp))
                        }
                    },
                    title = "현 위치",
                    subtitle = "현재 위치 사용",
                    isHighlighted = true,
                    testTag = "home_autoLocationCard",
                    onClick = {
                        viewModel.setLocationMode(LocationMode.AUTO)
                        viewModel.navigateTo(AppPage.REGION)
                        viewModel.refreshLocationInRegionScreen()
                    }
                )
                HomeLocationCard(
                    icon = {
                        ReferenceIconWell(size = 50.dp) {
                            IconSearch(tint = CharcoalText, modifier = Modifier.size(24.dp))
                        }
                    },
                    title = "지역 선택",
                    subtitle = "직접 지역 선택",
                    isHighlighted = false,
                    testTag = "home_manualRegionCard",
                    onClick = {
                        viewModel.setLocationMode(LocationMode.MANUAL)
                        viewModel.navigateTo(AppPage.REGION)
                    }
                )
            }
        }
    }
}
@Composable
private fun HomeLocationCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    isHighlighted: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .testTag(testTag)
            .shadow(elevation = 3.dp, shape = shape, ambientColor = CaramelDeep.copy(alpha = 0.06f), spotColor = CaramelDeep.copy(alpha = 0.06f))
            .background(color = if (isHighlighted) SelectionMint else Ivory, shape = shape)
            .border(width = 1.dp, color = CanvasLineAlpha80, shape = shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icon()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = AppTypography.headline)
            Text(text = subtitle, style = AppTypography.subheadline)
        }
        Box(modifier = Modifier.weight(1f))
        IconChevronRight(tint = CharcoalText, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun HomeHeroCta(viewModel: MainViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val railWidth = maxWidth
        val heroHeight = railWidth * (300f / 400f)
        Box(modifier = Modifier.fillMaxWidth().height(heroHeight)) {
            LunchHeroView(modifier = Modifier.fillMaxSize())

            val buttonWidth = railWidth * 0.46f
            val buttonHeight = railWidth * 0.15f
            val offsetX = railWidth * 0.0825f
            val offsetY = railWidth * 0.4075f
            val cornerRadius = buttonWidth * 0.075f
            val fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                (railWidth * 0.04f).toSp()
            }

            Row(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .width(buttonWidth)
                    .height(buttonHeight)
                    .testTag("home_recommendCta")
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(cornerRadius))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(androidx.compose.ui.graphics.Color.White, Ivory)
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .border(1.dp, CanvasLineAlpha80, RoundedCornerShape(cornerRadius))
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(role = Role.Button) {
                        viewModel.setSituationFilter(SituationFilter.ALL)
                        val lat = viewModel.currentLatitude.value
                        val lng = viewModel.currentLongitude.value
                        if (viewModel.locationMode.value == LocationMode.MANUAL && lat != null && lng != null) {
                            viewModel.startManualRecommendation(lat, lng, viewModel.currentRegionName.value)
                        } else if (viewModel.locationMode.value == LocationMode.MANUAL) {
                            viewModel.navigateTo(AppPage.REGION)
                        } else {
                            viewModel.startAutoRecommendation()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    railWidth * 0.045f,
                    Alignment.CenterHorizontally
                )
            ) {
                Text(
                    text = "메뉴 추천 받기",
                    fontSize = fontSize,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = CharcoalText
                )
                IconChevronRight(tint = CharcoalText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun SituationFilterRow(
    selectedFilter: SituationFilter,
    onSelectFilter: (SituationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .testTag("home_situationFilterRow"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SituationFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .testTag("home_situationFilter_${filter.name.lowercase()}")
                    .shadow(
                        elevation = if (isSelected) 2.dp else 0.dp,
                        shape = shape,
                        ambientColor = CaramelDeep.copy(alpha = 0.08f),
                        spotColor = CaramelDeep.copy(alpha = 0.08f)
                    )
                    .background(if (isSelected) SelectionMint else Ivory, shape)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) AccentRed.copy(alpha = 0.65f) else CanvasLineAlpha80,
                        shape = shape
                    )
                    .clip(shape)
                    .clickable(role = Role.Button) { onSelectFilter(filter) }
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.displayName,
                    style = AppTypography.caption.copy(
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (isSelected) AccentRed else CharcoalText
                    )
                )
            }
        }
    }
}
