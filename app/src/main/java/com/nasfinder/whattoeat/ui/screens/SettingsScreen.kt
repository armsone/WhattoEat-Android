package com.nasfinder.whattoeat.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.model.ReminderLeadTime
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.CanvasLineAlpha80
import com.nasfinder.whattoeat.theme.CaramelDeep
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.SelectionMint
import com.nasfinder.whattoeat.ui.components.AppToggle
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.IconChevronDown
import com.nasfinder.whattoeat.ui.components.IconBell
import com.nasfinder.whattoeat.ui.components.IconCamera
import com.nasfinder.whattoeat.ui.components.IconCheckCircle
import com.nasfinder.whattoeat.ui.components.IconForkKnife
import com.nasfinder.whattoeat.ui.components.IconGear
import com.nasfinder.whattoeat.ui.components.IconInformation
import com.nasfinder.whattoeat.ui.components.IconMap
import com.nasfinder.whattoeat.ui.components.IconNavigationArrow
import com.nasfinder.whattoeat.ui.components.IconPin
import com.nasfinder.whattoeat.ui.components.IconStorefront
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.ReferenceCard
import com.nasfinder.whattoeat.viewmodel.MainViewModel
import com.nasfinder.whattoeat.update.DirectUpdateManager
import com.nasfinder.whattoeat.update.DirectUpdateSettings

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val updateManager = DirectUpdateManager.get(context)
    val locationPermissionStatus by viewModel.locationPermissionStatus.collectAsState()
    val selectedProvider by viewModel.selectedMapProvider.collectAsState()
    val notifyEnabled by viewModel.lunchNotifyEnabled.collectAsState()
    val lunchHour by viewModel.lunchHour.collectAsState()
    val lunchMinute by viewModel.lunchMinute.collectAsState()
    val leadTime by viewModel.lunchLeadTime.collectAsState()
    val copyrightExpanded by viewModel.isCopyrightExpanded.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CompactHeader(
            title = "설정",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconGear(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(18.dp)) } },
            onCloseClick = { viewModel.closeSettings() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Location permission
            ReferenceCard(modifier = Modifier.fillMaxWidth().testTag("settings_locationPermission")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceIconWell(size = 30.dp) { IconNavigationArrow(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(15.dp)) }
                    Text(text = "위치 권한", style = AppTypography.sectionTitle)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (locationPermissionStatus == "사용 중") {
                            IconCheckCircle(tint = CharcoalText, modifier = Modifier.size(18.dp))
                        } else {
                            IconInformation(tint = CharcoalText, modifier = Modifier.size(18.dp))
                        }
                        Text(text = locationPermissionStatus, style = AppTypography.rowTitle)
                    }
                    Box(
                        modifier = Modifier
                            .background(com.nasfinder.whattoeat.theme.AccentRed.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                            .clickable(role = Role.Button) {
                            if (locationPermissionStatus == "사용 중") {
                                openAppSettings(context)
                            } else {
                                viewModel.requestLocationPermission?.invoke() ?: openAppSettings(context)
                            }
                        }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (locationPermissionStatus == "아직 선택하지 않음") "허용하기" else "설정 열기",
                            style = AppTypography.rowTitle.copy(color = com.nasfinder.whattoeat.theme.AccentRed)
                        )
                    }
                }
                Text(
                    text = "현재 위치를 다시 잡고 주변 음식점을 찾을 때만 사용해요.",
                    style = AppTypography.caption2,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Map providers
            ReferenceCard(modifier = Modifier.fillMaxWidth().testTag("settings_mapProviders")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceIconWell(size = 30.dp) { IconMap(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(15.dp)) }
                    Text(text = "길 찾기 지도", style = AppTypography.sectionTitle)
                }
                Row(
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapProvider.entries.forEach { provider ->
                        MapProviderCell(
                            provider = provider,
                            isSelected = selectedProvider == provider,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setMapProvider(provider) }
                        )
                    }
                }
                Text(
                    text = "음식점 지도를 누르면 여기서 고른 지도로 열어요.",
                    style = AppTypography.caption2,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Lunch reminder
            ReferenceCard(modifier = Modifier.fillMaxWidth().testTag("settings_lunchReminder")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceIconWell(size = 30.dp) { IconBell(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(15.dp)) }
                    Text(text = "점심 알림", style = AppTypography.sectionTitle)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "알림 받기", style = AppTypography.rowTitle)
                    AppToggle(
                        checked = notifyEnabled,
                        onCheckedChange = { viewModel.setLunchNotifyEnabled(it, context) },
                        modifier = Modifier.testTag("settings_notifyToggle")
                    )
                }
                AnimatedVisibility(
                    visible = notifyEnabled,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(220)) + shrinkVertically(tween(220))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CanvasLineAlpha80))
                        BoxWithConstraints(modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) {
                            val isWide = maxWidth >= 360.dp
                            val leadTimeChips: @Composable () -> Unit = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ReminderLeadTime.entries.forEach { lead ->
                                        LeadTimeChip(
                                            lead = lead,
                                            isSelected = leadTime == lead,
                                            onClick = { viewModel.setLunchLeadTime(lead, context) }
                                        )
                                    }
                                }
                            }
                            val lunchTimeClickModifier = Modifier
                                .testTag("settings_lunchTimeRow")
                                .clickable(role = Role.Button) {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> viewModel.setLunchTime(hour, minute, context) },
                                        lunchHour,
                                        lunchMinute,
                                        true
                                    ).show()
                                }
                            if (isWide) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = lunchTimeClickModifier,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "점심시간", style = AppTypography.rowTitle)
                                        Text(text = "%02d:%02d".format(lunchHour, lunchMinute), style = AppTypography.rowTitle)
                                    }
                                    leadTimeChips()
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().then(lunchTimeClickModifier),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "점심시간", style = AppTypography.rowTitle)
                                        Text(text = "%02d:%02d".format(lunchHour, lunchMinute), style = AppTypography.rowTitle)
                                    }
                                    leadTimeChips()
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "점심시간 전에 오늘의 추천을 알려드려요.\n알림은 이 기기에서만 울려요.",
                    style = AppTypography.caption2,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // Photo copyright
            ReferenceCard(modifier = Modifier.fillMaxWidth().testTag("settings_copyright")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceIconWell(size = 30.dp) { IconCamera(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(15.dp)) }
                    Text(text = "사진 출처와 이용 조건", style = AppTypography.sectionTitle)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_copyrightToggle")
                        .clickable(role = Role.Button) { viewModel.toggleCopyrightExpanded() }
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "카피라이트 안내", style = AppTypography.rowTitle)
                    IconChevronDown(
                        tint = CharcoalText,
                        modifier = Modifier.rotate(if (copyrightExpanded) 180f else 0f)
                    )
                }
                AnimatedVisibility(
                    visible = copyrightExpanded,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(220)) + shrinkVertically(tween(220))
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CanvasLineAlpha80))
                        CopyrightRow(
                            icon = { IconForkKnife(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(16.dp)) },
                            title = "메뉴 예시 사진",
                            description = "Openverse에서 상업적으로 사용할 수 있는 CC0, PDM, CC BY 사진만 사용해요."
                        )
                        CopyrightRow(
                            icon = { IconStorefront(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(16.dp)) },
                            title = "실제 식당 사진",
                            description = "한국관광공사 사진은 식당명과 위치가 정확히 맞을 때만 해당 식당 사진으로 사용해요."
                        )
                        CopyrightRow(
                            icon = { IconInformation(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(16.dp)) },
                            title = "사진별 상세 정보",
                            description = "각 사진의 ‘사진 정보’에서 저작자, 원문과 이용 조건을 확인할 수 있어요."
                        )
                    }
                }
                Text(
                    text = "사진의 출처와 사용 기준을 한곳에서 확인해요.",
                    style = AppTypography.caption2,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            ReferenceCard(modifier = Modifier.fillMaxWidth().testTag("settings_updates")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceIconWell(size = 30.dp) { IconInformation(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(15.dp)) }
                    Text(text = "앱 업데이트", style = AppTypography.sectionTitle)
                }
                DirectUpdateSettings(updateManager)
            }

        }
    }
}

@Composable
private fun MapProviderCell(
    provider: MapProvider,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(64.dp)
            .testTag("settings_mapProvider_${provider.name.lowercase()}")
            .background(
                if (isSelected) SelectionMint else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) com.nasfinder.whattoeat.theme.AccentRed.copy(alpha = 0.55f) else CanvasLineAlpha80.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = provider.iconRes),
            contentDescription = provider.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = provider.shortName,
            style = AppTypography.providerLabel.copy(
                color = if (isSelected) com.nasfinder.whattoeat.theme.AccentRed else CharcoalText
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LeadTimeChip(lead: ReminderLeadTime, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .testTag("settings_leadTime_${lead.name.lowercase()}")
            .background(if (isSelected) SelectionMint else Ivory, RoundedCornerShape(10.dp))
            .border(1.dp, (if (isSelected) com.nasfinder.whattoeat.theme.AccentRed else CanvasLineAlpha80).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = lead.label, style = AppTypography.caption)
    }
}

@Composable
private fun CopyrightRow(icon: @Composable () -> Unit, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.padding(top = 2.dp)) { icon() }
        Column {
            Text(text = title, style = AppTypography.copyrightTitle)
            Text(text = description, style = AppTypography.copyrightBody, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
