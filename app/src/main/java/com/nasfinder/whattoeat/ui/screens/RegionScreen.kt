package com.nasfinder.whattoeat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.LocationMode
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLineAlpha80
import com.nasfinder.whattoeat.theme.CharcoalSoft
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.SelectionMint
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.IconChevronRight
import com.nasfinder.whattoeat.ui.components.IconCrosshair
import com.nasfinder.whattoeat.ui.components.IconMap
import com.nasfinder.whattoeat.ui.components.IconNavigationArrow
import com.nasfinder.whattoeat.ui.components.IconSearch
import com.nasfinder.whattoeat.ui.components.IconStar
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.viewmodel.MainViewModel

@Composable
fun RegionScreen(viewModel: MainViewModel) {
    val locationMode by viewModel.locationMode.collectAsState()
    val statusText by viewModel.regionStatusText.collectAsState()
    val nearbyRegions by viewModel.nearbyRegions.collectAsState()
    val frequentRegions by viewModel.frequentRegions.collectAsState()
    val shouldFocusSearch by viewModel.matchupRegionSearchFocused.collectAsState()
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (nearbyRegions.isEmpty()) {
            viewModel.refreshLocationInRegionScreen()
        }
    }

    LaunchedEffect(shouldFocusSearch) {
        if (shouldFocusSearch) searchFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                focusManager.clearFocus()
            }
    ) {
        CompactHeader(
            title = "지역 선택",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconMap(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(18.dp)) } },
            onGearClick = { viewModel.openSettings() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 8.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Mode split
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Ivory, RoundedCornerShape(18.dp))
                    .border(1.dp, CanvasLineAlpha80, RoundedCornerShape(18.dp))
            ) {
                ModeHalf(
                    text = "지역 지정",
                    icon = { tint -> IconSearch(tint = tint, modifier = Modifier.size(16.dp)) },
                    isSelected = locationMode == LocationMode.MANUAL,
                    testTag = "region_mode_manual",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setLocationMode(LocationMode.MANUAL) }
                )
                ModeHalf(
                    text = "현 위치",
                    icon = { tint -> IconNavigationArrow(tint = tint, modifier = Modifier.size(16.dp)) },
                    isSelected = locationMode == LocationMode.AUTO,
                    unselectedIconColor = AccentRed,
                    testTag = "region_mode_current",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setLocationMode(LocationMode.AUTO)
                        viewModel.refreshLocationInRegionScreen()
                    }
                )
            }

            Text(
                text = statusText,
                style = AppTypography.caption,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("region_statusText")
            )

            // Search row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ivory, RoundedCornerShape(22.dp))
                    .border(1.dp, CanvasLineAlpha80, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it; searchError = null },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("region_searchField")
                        .focusRequester(searchFocusRequester)
                        .onFocusChanged { },
                    singleLine = true,
                    textStyle = TextStyle(color = CharcoalText, fontSize = AppTypography.body.fontSize),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.searchRegion(searchText) { success, error ->
                            if (!success) searchError = error
                        }
                        focusManager.clearFocus()
                    }),
                    decorationBox = { inner ->
                        if (searchText.isEmpty()) {
                            Text(
                                text = "지역명으로 검색 (예: 강남, 판교)",
                                style = AppTypography.body.copy(color = CharcoalSoft)
                            )
                        }
                        inner()
                    }
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("region_searchButton")
                        .clickable(role = Role.Button) {
                            viewModel.searchRegion(searchText) { success, error ->
                                if (!success) searchError = error
                            }
                            focusManager.clearFocus()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    IconSearch(tint = CharcoalText)
                }
            }

            searchError?.let { error ->
                Text(
                    text = error,
                    style = AppTypography.caption.copy(color = com.nasfinder.whattoeat.theme.AccentRed),
                    modifier = Modifier.testTag("region_searchError")
                )
            }

            // Nearby
            RegionSection(
                title = "내 주변",
                icon = { IconCrosshair(tint = CharcoalText, modifier = Modifier.size(15.dp)) }
            ) {
                if (nearbyRegions.isEmpty()) {
                    EmptySectionRow(text = "현 위치를 확인하면 주변 지역을 보여드려요.")
                } else {
                    nearbyRegions.forEachIndexed { index, region ->
                        if (index > 0) RegionRowDivider()
                        RegionRow(
                            name = region.name,
                            testTag = "region_nearby_${region.name}",
                            showCurrentLocationBadge = index == 0,
                            onClick = { viewModel.startManualRecommendation(region.lat, region.lng, region.name) }
                        )
                    }
                }
            }

            // Frequent
            RegionSection(
                title = "자주 찾는 지역",
                icon = { IconStar(tint = CharcoalText, modifier = Modifier.size(15.dp)) }
            ) {
                if (frequentRegions.isEmpty()) {
                    EmptySectionRow(text = "아직 자주 찾는 지역이 없어요.")
                } else {
                    frequentRegions.forEachIndexed { index, region ->
                        if (index > 0) RegionRowDivider()
                        RegionRow(
                            name = region.name,
                            testTag = "region_frequent_${region.name}",
                            showCurrentLocationBadge = false,
                            onClick = { viewModel.startManualRecommendation(region.lat, region.lng, region.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeHalf(
    text: String,
    icon: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
    isSelected: Boolean,
    testTag: String,
    unselectedIconColor: androidx.compose.ui.graphics.Color = CharcoalText,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconTint = if (isSelected) AccentRed else unselectedIconColor
    val textTint = if (isSelected) AccentRed else CharcoalText
    Row(
        modifier = modifier
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick)
            .background(if (isSelected) SelectionMint else Ivory, RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            icon(iconTint)
            Text(text = text, style = AppTypography.rowTitle.copy(color = textTint))
        }
    }
}

@Composable
private fun RegionSection(title: String, icon: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(text = title, style = AppTypography.sectionTitle)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ivory, RoundedCornerShape(18.dp))
                .border(1.dp, CanvasLineAlpha80, RoundedCornerShape(18.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun EmptySectionRow(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
        Text(text = text, style = AppTypography.supporting)
    }
}

@Composable
private fun RegionRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .height(1.dp)
            .background(CanvasLineAlpha80)
    )
}

@Composable
private fun RegionRow(name: String, testTag: String, showCurrentLocationBadge: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = name, style = AppTypography.rowTitle, modifier = Modifier.weight(1f))
        if (showCurrentLocationBadge) {
            Box(
                modifier = Modifier
                    .background(SelectionMint, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = "현 위치", style = AppTypography.caption2)
            }
        }
        IconChevronRight(tint = CharcoalText, modifier = Modifier.size(14.dp))
    }
}
