package com.example.qrscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrscanner.AppViewModel
import com.example.qrscanner.model.ScanEntry
import com.example.qrscanner.model.ScanKind
import com.example.qrscanner.ui.theme.AppColors
import com.example.qrscanner.ui.theme.MonoFont

private sealed interface Row {
    data class Header(val label: String) : Row
    data class Item(val entry: ScanEntry) : Row
}

@Composable
fun HistoryScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }

    val filtered = vm.history.filter { e ->
        query.isBlank() ||
            e.scan.historyTitle.contains(query, true) ||
            e.scan.historySubtitle.contains(query, true) ||
            e.scan.raw.contains(query, true)
    }

    val rows = buildList {
        var lastDay: String? = null
        filtered.forEach { e ->
            if (e.dayLabel != lastDay) {
                add(Row.Header(e.dayLabel))
                lastDay = e.dayLabel
            }
            add(Row.Item(e))
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(AppColors.Panel)
            .statusBarsPadding(),
    ) {
        // page head
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 12.dp)) {
            Text("History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            Text(
                "Scans from the last 7 days",
                fontSize = 13.sp,
                color = AppColors.TextDim,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // search
        Row(
            Modifier
                .padding(horizontal = 22.dp)
                .padding(bottom = 14.dp)
                .fillMaxWidth()
                .background(AppColors.Card, RoundedCornerShape(13.dp))
                .border(1.dp, AppColors.Line, RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.Search, "Search", tint = AppColors.TextDim, modifier = Modifier.size(18.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = AppColors.Text, fontSize = 14.sp),
                cursorBrush = SolidColor(AppColors.Ink),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Search history…", color = AppColors.TextDim, fontSize = 14.sp)
                        }
                        inner()
                    }
                },
            )
        }

        if (rows.isEmpty()) {
            HistoryEmptyState(hasSearchQuery = query.isNotBlank())
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(rows.size) { index ->
                    when (val row = rows[index]) {
                        is Row.Header -> Text(
                            row.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextDim,
                            modifier = Modifier.padding(top = 8.dp, start = 2.dp),
                        )
                        is Row.Item -> HistoryItem(row.entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(hasSearchQuery: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.History,
            contentDescription = null,
            tint = AppColors.TextDim,
            modifier = Modifier.size(48.dp),
        )
        Text(
            if (hasSearchQuery) "No matching scans" else "No scans yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Text,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            if (hasSearchQuery) {
                "Try a different search term"
            } else {
                "Codes you scan will appear here for 7 days"
            },
            fontSize = 13.sp,
            color = AppColors.TextDim,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun HistoryItem(entry: ScanEntry) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Card, RoundedCornerShape(15.dp))
            .border(1.dp, AppColors.Line, RoundedCornerShape(15.dp))
            .clickable { }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        KindIcon(entry.scan.kind)
        Column(Modifier.weight(1f)) {
            Text(
                entry.scan.historyTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = if (entry.scan.kind == ScanKind.PRODUCT) MonoFont else null,
            )
            Text(
                entry.scan.historySubtitle,
                fontSize = 12.sp,
                color = AppColors.TextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Text(entry.time, fontSize = 11.sp, color = AppColors.TextDim)
    }
}

@Composable
private fun KindIcon(kind: ScanKind) {
    val bg = when (kind) {
        ScanKind.URL, ScanKind.EMAIL -> AppColors.IconBgUrl
        ScanKind.WIFI -> AppColors.IconBgWifi
        ScanKind.PRODUCT -> AppColors.IconBgBar
        else -> AppColors.IconBgTxt
    }
    val tint = when (kind) {
        ScanKind.URL, ScanKind.EMAIL -> Color(0xFF2E6BE6)
        ScanKind.WIFI -> AppColors.Teal
        ScanKind.PRODUCT -> Color(0xFFE07A1F)
        else -> Color(0xFF7A5CE0)
    }
    Box(
        Modifier.size(42.dp).background(bg, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (kind == ScanKind.PRODUCT) {
            Text("|||", fontFamily = MonoFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = tint)
        } else {
            Icon(iconFor(kind), null, tint = tint, modifier = Modifier.size(21.dp))
        }
    }
}

private fun iconFor(kind: ScanKind): ImageVector = when (kind) {
    ScanKind.URL -> Icons.Rounded.Language
    ScanKind.WIFI -> Icons.Rounded.Wifi
    ScanKind.PHONE -> Icons.Rounded.Call
    ScanKind.EMAIL -> Icons.Rounded.Mail
    ScanKind.GEO -> Icons.Rounded.Place
    ScanKind.SMS -> Icons.Rounded.Sms
    else -> Icons.Rounded.Description
}
