package com.nasfinder.whattoeat.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nasfinder.whattoeat.model.MapProvider
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.CharcoalText
import com.nasfinder.whattoeat.theme.CanvasLine
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.theme.MintBase

/**
 * App-owned alert/sheet chrome: SwiftUI alert replica, not Material AlertDialog styling.
 */
@Composable
fun AppAlertContainer(
    onDismissRequest: () -> Unit,
    testTag: String = "appAlert",
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x4D000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .testTag(testTag)
                    .widthIn(min = 270.dp, max = 320.dp)
                    .background(color = Ivory, shape = RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun MissingMapAlertDialog(
    provider: MapProvider,
    onInstall: () -> Unit,
    onChooseOther: () -> Unit,
    onCancel: () -> Unit
) {
    AppAlertContainer(onDismissRequest = onCancel, testTag = "missingMapAlert") {
        Text(
            text = "${provider.displayName}가 필요해요",
            style = AppTypography.sectionTitle,
            modifier = Modifier.testTag("missingMapAlert_title")
        )
        Text(
            text = "설치하거나, 이미 있는 다른 지도로 이 식당을 열 수 있어요.",
            style = AppTypography.supporting,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        AlertActionButton(text = "설치하기", onClick = onInstall, testTag = "missingMapAlert_install")
        AlertActionButton(text = "다른 지도 선택", onClick = onChooseOther, testTag = "missingMapAlert_chooseOther")
        AlertActionButton(text = "취소", onClick = onCancel, isDestructiveOrCancel = true, testTag = "missingMapAlert_cancel")
    }
}

@Composable
fun OtherMapPickerDialog(
    installedProviders: List<MapProvider>,
    onSelect: (MapProvider) -> Unit,
    onCancel: () -> Unit
) {
    AppAlertContainer(onDismissRequest = onCancel, testTag = "otherMapPicker") {
        Text(text = "다른 지도 선택", style = AppTypography.sectionTitle)
        installedProviders.forEach { provider ->
            AlertActionButton(
                text = provider.displayName,
                onClick = { onSelect(provider) },
                testTag = "otherMapPicker_${provider.name.lowercase()}"
            )
        }
        AlertActionButton(text = "취소", onClick = onCancel, isDestructiveOrCancel = true, testTag = "otherMapPicker_cancel")
    }
}

@Composable
fun AppleMapFailureAlertDialog(onDismiss: () -> Unit) {
    AppAlertContainer(onDismissRequest = onDismiss, testTag = "appleMapFailureAlert") {
        Text(text = "Apple 지도를 열 수 없어요", style = AppTypography.sectionTitle)
        AlertActionButton(text = "확인", onClick = onDismiss, testTag = "appleMapFailureAlert_confirm")
    }
}

@Composable
fun BusinessInfoAlertDialog(onDismiss: () -> Unit) {
    AppAlertContainer(onDismissRequest = onDismiss, testTag = "businessInfoAlert") {
        Text(text = "영업 정보", style = AppTypography.sectionTitle)
        Text(
            text = "식당 정보는 지도 제공처의 최신 상태와 다를 수 있어요. 방문하기 전에 영업 여부와 실제 메뉴를 지도에서 한 번 확인해 주세요.",
            style = AppTypography.supporting,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        AlertActionButton(text = "확인", onClick = onDismiss, testTag = "businessInfoAlert_confirm")
    }
}

@Composable
fun NotificationDeniedAlertDialog(onDismiss: () -> Unit) {
    AppAlertContainer(onDismissRequest = onDismiss, testTag = "notificationDeniedAlert") {
        Text(text = "알림이 꺼져 있어요", style = AppTypography.sectionTitle)
        Text(
            text = "기기 설정의 알림에서 '오늘 뭐 먹지??'를 허용해 주세요.",
            style = AppTypography.supporting,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        AlertActionButton(text = "확인", onClick = onDismiss, testTag = "notificationDeniedAlert_confirm")
    }
}

@Composable
fun LocationDeniedAlertDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertContainer(onDismissRequest = onDismiss, testTag = "locationDeniedAlert") {
        Text(
            text = "위치 권한이 꺼져 있어요",
            style = AppTypography.sectionTitle,
            modifier = Modifier.testTag("locationDeniedAlert_title")
        )
        Text(
            text = "현재 위치 주변의 맛있는 한 끼를 추천받으려면 기기 설정에서 위치 권한을 켜 주세요.",
            style = AppTypography.supporting,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        AlertActionButton(text = "설정 열기", onClick = onOpenSettings, testTag = "locationDeniedAlert_settings")
        AlertActionButton(text = "취소", onClick = onDismiss, isDestructiveOrCancel = true, testTag = "locationDeniedAlert_cancel")
    }
}

fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Safe fallback per no-crash policy
    }
}

@Composable
fun PhotoInfoSheet(
    information: com.nasfinder.whattoeat.model.PhotoInformation,
    onOpenUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x4D000000))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .testTag("photoInfoSheet")
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .background(color = MintBase, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                val isExample = information.isCategoryExample
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReferenceIconWell(size = 38.dp) {
                        if (isExample) {
                            IconForkKnife(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(18.dp))
                        } else {
                            IconPhoto(tint = com.nasfinder.whattoeat.theme.AccentRed, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(
                        text = if (isExample) "메뉴 예시 사진" else "사진 정보",
                        style = AppTypography.title3,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "닫기", style = AppTypography.rowTitle, modifier = Modifier.clickable(onClick = onDismiss))
                }
                Text(
                    text = if (isExample) {
                        "음식 종류를 보여 주는 예시이며, 이 식당에서 촬영한 사진은 아니에요."
                    } else {
                        "사진 제공처와 원문을 확인할 수 있어요."
                    },
                    style = AppTypography.subheadline
                )
                information.title?.takeIf { it.isNotEmpty() }?.let { PhotoInfoRow(label = "제목", value = it) }
                information.creator?.takeIf { it.isNotEmpty() }?.let { PhotoInfoRow(label = "사진", value = it) }
                information.license?.takeIf { it.isNotEmpty() }?.let { PhotoInfoRow(label = "이용 조건", value = it.uppercase()) }
                information.attribution?.takeIf { it.isNotEmpty() }?.let { PhotoInfoRow(label = "제공", value = it) }
                information.provider?.takeIf { it.isNotEmpty() }?.let { PhotoInfoRow(label = "출처", value = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    safeHttps(information.sourceUrl)?.let { url ->
                        PhotoInfoLinkRow(label = "", linkText = "원문 보기", url = url, onOpenUrl = onOpenUrl)
                    }
                    safeHttps(information.creatorUrl)?.let { url ->
                        PhotoInfoLinkRow(label = "", linkText = "작가 보기", url = url, onOpenUrl = onOpenUrl)
                    }
                    safeHttps(information.licenseUrl)?.let { url ->
                        PhotoInfoLinkRow(label = "", linkText = "이용 조건", url = url, onOpenUrl = onOpenUrl)
                    }
                }
            }
        }
    }
}

private fun safeHttps(raw: String?): String? = raw?.takeIf {
    runCatching { android.net.Uri.parse(it).scheme.equals("https", ignoreCase = true) }.getOrDefault(false)
}

@Composable
private fun PhotoInfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold, color = AccentRed),
            modifier = Modifier.width(58.dp)
        )
        Text(text = value, style = AppTypography.subheadline.copy(color = CharcoalText), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhotoInfoLinkRow(label: String, linkText: String, url: String, onOpenUrl: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (label.isNotEmpty()) {
            Text(text = label, style = AppTypography.caption2)
        }
        Text(
            text = linkText,
            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold, color = CharcoalText),
            modifier = Modifier
                .testTag("photoInfoSheet_link")
                .heightIn(min = 38.dp)
                .clickable { onOpenUrl(url) }
                .background(Ivory, RoundedCornerShape(50))
                .border(1.dp, CanvasLine, RoundedCornerShape(50))
                .padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun AlertActionButton(
    text: String,
    onClick: () -> Unit,
    isDestructiveOrCancel: Boolean = false,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.rowTitle.copy(
                color = if (isDestructiveOrCancel) CharcoalText.copy(alpha = 0.6f) else CharcoalText
            )
        )
    }
}
