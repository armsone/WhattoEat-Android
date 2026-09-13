package com.nasfinder.whattoeat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.nasfinder.whattoeat.data.PublicDiningPriority
import com.nasfinder.whattoeat.model.Restaurant
import com.nasfinder.whattoeat.theme.AccentRed
import com.nasfinder.whattoeat.theme.AppTypography
import com.nasfinder.whattoeat.theme.SelectionMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicDiningRecord(restaurant: Restaurant, showDetails: Boolean = false) {
    val record = PublicDiningPriority.match(restaurant) ?: return
    var expanded by remember(restaurant.id) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Column(Modifier.then(if (showDetails) Modifier.clickable { expanded = true } else Modifier)) {
        Text("공공기관 이용 기록", style = AppTypography.caption2, color = AccentRed,
            modifier = Modifier.background(SelectionMint, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp))
        if (showDetails) Text("최근 결제 ${record.lastPaymentDate} · 기록 보기", style = AppTypography.caption2)
    }
    if (expanded) ModalBottomSheet(onDismissRequest = { expanded = false }) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("공공기관 이용 기록", style = AppTypography.title2)
            Text("공공기관의 식사 이용 기록을 식당 이름·주소와 연결했어요. 일부 누락 주소는 공식 사업장 자료로 보완했어요. 맛이나 안전, 현재 영업 여부를 뜻하지는 않아요.", style = AppTypography.subheadline)
            Text("식당  ${restaurant.name}", style = AppTypography.subheadline)
            record.region?.let { Text("지역  $it", style = AppTypography.subheadline) }
            Text("기록 주소  ${record.address}", style = AppTypography.subheadline)
            if (record.departmentCount > 0) Text("이용 부서  ${record.departmentCount}곳", style = AppTypography.subheadline)
            Text("최근 결제  ${record.lastPaymentDate}", style = AppTypography.subheadline)
            Text("수집 범위  ${record.coverageDescription}", style = AppTypography.subheadline)
            Text("집계일  ${record.generatedAt.take(10)}", style = AppTypography.caption)
            TextButton(onClick = { uriHandler.openUri(record.sourceURL) }) { Text("공식 원문 보기") }
            record.sourceURLs.distinct().take(3).forEachIndexed { i, url ->
                TextButton(onClick = { uriHandler.openUri(url) }) { Text("자료 출처 ${i + 1}") }
            }
            Text("표시 기준: 공공기관 식사 이용 1건 이상, 최근 18개월 안의 결제 기록. 방문 전 영업 여부는 지도에서 확인해 주세요.", style = AppTypography.caption)
            TextButton(onClick = { expanded = false }) { Text("닫기") }
            Spacer(Modifier.height(12.dp))
        }
    }
}
