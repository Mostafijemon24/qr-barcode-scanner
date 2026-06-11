package com.example.qrscanner.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrscanner.model.ClassifiedScan
import com.example.qrscanner.model.PrimaryAction
import com.example.qrscanner.model.ScanKind
import com.example.qrscanner.model.WifiParser
import com.example.qrscanner.ui.theme.AppColors
import com.example.qrscanner.ui.theme.MonoFont

/**
 * Bottom result sheet that slides up over the camera when a code is detected.
 * Open / Copy / Share / Close are wired to real Android intents.
 */
@Composable
fun ResultSheet(
    scan: ClassifiedScan?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the last value so the slide-out animation still has content to draw.
    var last by remember { mutableStateOf<ClassifiedScan?>(null) }
    if (scan != null) last = scan
    val visible = scan != null

    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.matchParentSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xCC0A1423))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onDismiss() }
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            last?.let { SheetContent(it, onDismiss) }
        }
    }
}

@Composable
private fun SheetContent(scan: ClassifiedScan, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun launch(intent: Intent) = try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast("No app can handle this")
    }

    val normalizedUrl = if (scan.raw.startsWith("http", true)) scan.raw else "https://${scan.raw}"

    fun doPrimary() = when (scan.primaryAction) {
        PrimaryAction.OPEN_LINK -> launch(Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)))
        PrimaryAction.CALL -> launch(Intent(Intent.ACTION_DIAL, Uri.parse(if (scan.raw.startsWith("tel:")) scan.raw else "tel:${scan.raw}")))
        PrimaryAction.EMAIL -> launch(Intent(Intent.ACTION_SENDTO, Uri.parse(if (scan.raw.startsWith("mailto:")) scan.raw else "mailto:${scan.raw}")))
        PrimaryAction.OPEN_MAP -> launch(Intent(Intent.ACTION_VIEW, Uri.parse(scan.raw)))
        PrimaryAction.SEND_SMS -> launch(Intent(Intent.ACTION_SENDTO, Uri.parse(if (scan.raw.startsWith("sms")) scan.raw else "smsto:${scan.raw}")))
        PrimaryAction.COPY_PASSWORD -> {
            clipboard.setText(AnnotatedString(WifiParser.password(scan.raw) ?: ""))
            toast("Password copied")
        }
        PrimaryAction.SEARCH_WEB -> launch(Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", scan.raw))
        PrimaryAction.NONE -> {
            clipboard.setText(AnnotatedString(scan.raw)); toast("Copied")
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Card, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .navigationBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 96.dp),
    ) {
        // grab handle
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .size(width = 42.dp, height = 4.dp)
                .background(AppColors.Line, RoundedCornerShape(50)),
        )

        // teal "detected" stamp
        Row(
            Modifier
                .background(AppColors.Teal.copy(alpha = 0.10f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.Teal, modifier = Modifier.size(15.dp))
            Text(
                if (scan.isBarcode) "Barcode detected" else "QR code detected",
                color = AppColors.Teal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            scan.heading,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Text,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )

        // payload box
        Column(
            Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .background(AppColors.Panel, RoundedCornerShape(12.dp))
                .border(1.dp, AppColors.Line, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                scan.payloadLabel,
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = AppColors.TextDim,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                payloadValue(scan),
                fontFamily = MonoFont,
                fontSize = 13.sp,
                color = AppColors.Text,
            )
        }

        // 2x2 action grid
        Column(
            Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetButton(scan.primaryAction.label, primary = true, modifier = Modifier.weight(1f)) { doPrimary() }
                SheetButton("Copy", modifier = Modifier.weight(1f)) {
                    clipboard.setText(AnnotatedString(scan.raw)); toast("Copied")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetButton("Share", modifier = Modifier.weight(1f)) {
                    launch(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, scan.raw),
                            "Share",
                        )
                    )
                }
                SheetButton("Close", modifier = Modifier.weight(1f)) { onDismiss() }
            }
        }
    }
}

private fun payloadValue(scan: ClassifiedScan): String = when (scan.kind) {
    ScanKind.WIFI -> buildString {
        append("Network: ${WifiParser.ssid(scan.raw) ?: "—"}")
        WifiParser.password(scan.raw)?.let { append("\nPassword: $it") }
    }
    else -> scan.raw
}

@Composable
private fun SheetButton(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .background(
                if (primary) AppColors.Ink else AppColors.Card,
                RoundedCornerShape(13.dp),
            )
            .border(
                1.dp,
                if (primary) AppColors.Ink else AppColors.Line,
                RoundedCornerShape(13.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (primary) AppColors.Card else AppColors.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
