package com.nasfinder.whattoeat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.ChoiceRecord
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.EmptyRecentIllustration
import com.nasfinder.whattoeat.ui.components.FoodImageView
import com.nasfinder.whattoeat.ui.components.IconHistory
import com.nasfinder.whattoeat.ui.components.IconPin
import com.nasfinder.whattoeat.ui.components.IconTrash
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.SwipeRevealRow
import com.nasfinder.whattoeat.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val records by viewModel.choiceRecords.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CompactHeader(
            title = "최근 한 끼",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconHistory(tint = AccentRed, modifier = Modifier.size(18.dp)) } },
            onGearClick = { viewModel.openSettings() }
        )

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 430.dp)
                    .padding(horizontal = 28.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyRecentIllustration()
                Text(
                    text = "첫 한 끼를 기다리고 있어요",
                    style = AppTypography.sectionTitle,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "오늘 메뉴를 고르면 맛있는 기억이\n여기에 차곡차곡 쌓여요.",
                    style = AppTypography.supporting,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val grouped = groupByRegion(records)
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                grouped.forEach { (region, groupRecords) ->
                    item(key = "header_$region") {
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconPin(tint = AccentRed, modifier = Modifier.size(16.dp))
                            Text(text = region, style = AppTypography.sectionTitle)
                        }
                    }
                    items(groupRecords, key = { it.date }) { record ->
                        HistoryRow(
                            record = record,
                            onDelete = { viewModel.deleteChoiceRecord(record) },
                            onPhotoInfo = viewModel::showPhotoInformation
                        )
                    }
                }
                item(key = "footer") {
                    Text(
                        text = "최근에 결정한 메뉴와 음식점은 이 기기에만 보관돼요.",
                        style = AppTypography.caption2,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

private fun groupByRegion(records: List<ChoiceRecord>): List<Pair<String, List<ChoiceRecord>>> {
    val ordered = records.sortedByDescending { it.date }
    val map = LinkedHashMap<String, MutableList<ChoiceRecord>>()
    ordered.forEach { record ->
        val key = record.region?.takeIf { it.isNotBlank() } ?: "이전 기록"
        map.getOrPut(key) { mutableListOf() }.add(record)
    }
    return map.map { it.key to it.value }
}

@Composable
private fun HistoryRow(
    record: ChoiceRecord,
    onDelete: () -> Unit,
    onPhotoInfo: (com.nasfinder.whattoeat.model.PhotoInformation) -> Unit
) {
    val dateFormat = remember(record.date) { SimpleDateFormat("yyyy년 M월 d일 a h:mm", Locale.KOREA) }
    SwipeRevealRow(
        onAction = onDelete,
        accessibilityActionLabel = "최근 한 끼 삭제",
        actionContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconTrash(tint = Ivory, modifier = Modifier.size(18.dp))
                Text(
                    text = "삭제",
                    style = AppTypography.caption2.copy(
                        color = Ivory,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        },
        modifier = Modifier.testTag("history_row_${record.date}")
    ) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val photoInformation = com.nasfinder.whattoeat.model.PhotoInformation(
            record.photoKind, record.photoProvider, record.photoSourceUrl, record.photoAttribution,
            record.photoCreator, record.photoCreatorUrl, record.photoLicense, record.photoLicenseUrl, record.photoTitle
        )
        FoodImageView(
            photoUrl = record.imageUrl,
            category = record.category ?: "",
            menu = record.menu,
            seed = record.restaurantId ?: record.restaurantName,
            photoInformation = photoInformation,
            onInfoClick = photoInformation.takeIf { it.hasDetails }?.let { { onPhotoInfo(it) } },
            modifier = Modifier.size(width = 82.dp, height = 76.dp),
            cornerRadius = 0.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.menu, style = AppTypography.headline, maxLines = 1)
            Text(text = record.restaurantName, style = AppTypography.subheadline, maxLines = 1)
            Text(text = dateFormat.format(Date(record.date)), style = AppTypography.caption)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onDelete)
                .testTag("history_row_${record.date}_delete"),
            contentAlignment = Alignment.Center
        ) {
            IconTrash(tint = AccentRed, modifier = Modifier.size(20.dp))
        }
      }
    }
}
