package com.example.qrscanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrscanner.ui.theme.AppColors

enum class Screen(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Rounded.QrCodeScanner),
    CREATE("Create", Icons.Rounded.Add),
    HISTORY("History", Icons.Rounded.History),
}

@Composable
fun BottomNav(
    current: Screen,
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(AppColors.NavBg, RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Screen.entries.forEach { screen ->
            NavItem(
                screen = screen,
                selected = screen == current,
                onClick = { onSelect(screen) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) AppColors.NavIconActive else AppColors.NavIconIdle
    Column(
        modifier
            .selectable(selected = selected, onClick = onClick)
            .background(
                if (selected) Color.White.copy(alpha = 0.10f) else Color.Transparent,
                RoundedCornerShape(15.dp),
            )
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(screen.icon, contentDescription = screen.label, tint = tint, modifier = Modifier.size(22.dp))
        Text(
            screen.label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}
