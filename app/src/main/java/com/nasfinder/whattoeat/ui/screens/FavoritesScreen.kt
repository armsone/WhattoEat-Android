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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.model.FavoriteRecord
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.Ivory
import com.nasfinder.whattoeat.ui.components.CompactHeader
import com.nasfinder.whattoeat.ui.components.EmptyFavoritesIllustration
import com.nasfinder.whattoeat.ui.components.FoodImageView
import com.nasfinder.whattoeat.ui.components.IconHeart
import com.nasfinder.whattoeat.ui.components.IconPin
import com.nasfinder.whattoeat.ui.components.IconTrash
import com.nasfinder.whattoeat.ui.components.ReferenceIconWell
import com.nasfinder.whattoeat.ui.components.SwipeRevealRow
import com.nasfinder.whattoeat.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesScreen(viewModel: MainViewModel) {
    val records by viewModel.favoriteRecords.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CompactHeader(
            title = "찜한 맛집",
            leftWell = { ReferenceIconWell(size = 34.dp) { IconHeart(tint = AccentRed, isFilled = true, modifier = Modifier.size(18.dp)) } },
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
                EmptyFavoritesIllustration()
                Text(
                    text = "첫 하트를 기다리고 있어요",
                    style = AppTypography.sectionTitle,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "추천에서 마음에 드는 곳을\n콕 눌러 주세요.",
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
                    items(groupRecords, key = { it.restaurantId }) { record ->
                        FavoriteRow(
                            record = record,
                            onRemove = { viewModel.deleteFavorite(record.restaurantId) },
                            onPhotoInfo = viewModel::showPhotoInformation
                        )
                    }
                }
            }
        }
    }
}

private fun groupByRegion(records: List<FavoriteRecord>): List<Pair<String, List<FavoriteRecord>>> {
    val ordered = records.sortedByDescending { it.date }
    val map = LinkedHashMap<String, MutableList<FavoriteRecord>>()
    ordered.forEach { record ->
        val key = record.region?.takeIf { it.isNotBlank() } ?: "이전 기록"
        map.getOrPut(key) { mutableListOf() }.add(record)
    }
    return map.map { it.key to it.value }
}

@Composable
private fun FavoriteRow(
    record: FavoriteRecord,
    onRemove: () -> Unit,
    onPhotoInfo: (com.nasfinder.whattoeat.model.PhotoInformation) -> Unit
) {
    val dateFormat = remember(record.date) { SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA) }
    SwipeRevealRow(
        onAction = onRemove,
        accessibilityActionLabel = "찜 해제",
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
        modifier = Modifier.testTag("favorites_row_${record.restaurantId}").semantics { stateDescription = "선택됨" }
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
            category = record.category,
            menu = record.category,
            seed = record.restaurantId,
            photoInformation = photoInformation,
            onInfoClick = photoInformation.takeIf { it.hasDetails }?.let { { onPhotoInfo(it) } },
            modifier = Modifier.size(width = 82.dp, height = 76.dp),
            cornerRadius = 0.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.restaurantName, style = AppTypography.headline, maxLines = 1)
            Text(text = record.category, style = AppTypography.subheadline, maxLines = 1)
            Text(text = dateFormat.format(Date(record.date)), style = AppTypography.caption)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onRemove)
                .testTag("favorites_row_${record.restaurantId}_remove"),
            contentAlignment = Alignment.Center
        ) {
            IconHeart(tint = AccentRed, isFilled = true, modifier = Modifier.size(22.dp))
        }
      }
    }
}
