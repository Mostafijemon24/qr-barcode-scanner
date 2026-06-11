package com.example.qrscanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.qrscanner.AppViewModel
import com.example.qrscanner.ScanFilter
import com.example.qrscanner.scanner.CameraPreview
import com.example.qrscanner.scanner.GalleryScanner
import com.example.qrscanner.ui.components.ResultSheet
import com.example.qrscanner.ui.components.Viewfinder
import com.example.qrscanner.ui.theme.AppColors

@Composable
fun ScanScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamera = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            GalleryScanner.scan(context, uri) { detection ->
                if (detection != null) vm.onGalleryDetection(detection)
                else Toast.makeText(context, "No code found in image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasCamera) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier.fillMaxSize().background(AppColors.Ink)) {
        // Live camera, or a dark gradient stand-in when there's no permission yet.
        if (hasCamera) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = vm.lensFacing,
                torchOn = vm.torchOn,
                zoomRatio = vm.zoomRatio,
                isPaused = vm.isPaused,
                onDetected = vm::onDetected,
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(AppColors.GradientTop, AppColors.Ink))
                    )
            )
        }

        // Legibility scrim over the camera feed.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to AppColors.Ink.copy(alpha = 0.55f),
                        0.22f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1f to AppColors.Ink.copy(alpha = 0.9f),
                    )
                )
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // ---- top bar ----
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 14.dp)
            ) {
                IconButton(
                    icon = if (vm.torchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    desc = "Flash",
                    modifier = Modifier.align(Alignment.CenterStart),
                ) { vm.toggleTorch() }

                Text(
                    "Scan",
                    color = AppColors.ScanTopText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                )

                IconButton(
                    icon = Icons.Rounded.PhotoLibrary,
                    desc = "Scan from gallery",
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) { galleryLauncher.launch("image/*") }
            }

            // ---- finder ----
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Viewfinder(showGhost = !hasCamera)
                    if (hasCamera) {
                        Text(
                            "Align the code inside the frame — it scans automatically",
                            color = AppColors.ScanHint,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(240.dp)
                                .padding(top = 40.dp),
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 36.dp),
                        ) {
                            Text(
                                "Camera access is needed to scan",
                                color = AppColors.ScanHint,
                                fontSize = 13.sp,
                            )
                            Box(
                                Modifier
                                    .padding(top = 12.dp)
                                    .background(AppColors.FinderStroke, RoundedCornerShape(12.dp))
                                    .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text("Allow camera", color = AppColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // ---- type chips ----
            Row(
                Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                ScanFilter.entries.forEach { f ->
                    TypeChip(label = f.label, selected = vm.filter == f) { vm.filter = f }
                }
            }

            // ---- actions ----
            Row(
                Modifier.fillMaxWidth().padding(bottom = 104.dp),
                horizontalArrangement = Arrangement.spacedBy(34.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SideAction(Icons.Rounded.ZoomIn, "Zoom") { vm.cycleZoom() }
                Shutter {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (vm.isPaused) vm.dismissResult()
                }
                SideAction(Icons.Rounded.Cameraswitch, "Flip") { vm.flipLens() }
            }
        }

        // ---- result sheet ----
        ResultSheet(scan = vm.currentResult, onDismiss = vm::dismissResult)
    }
}

@Composable
private fun IconButton(
    icon: ImageVector,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .size(38.dp)
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, desc, tint = AppColors.ScanTopText, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(
                if (selected) AppColors.FinderStroke else Color.Transparent,
                RoundedCornerShape(50),
            )
            .border(
                1.dp,
                if (selected) AppColors.FinderStroke else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(50),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            color = if (selected) AppColors.Ink else AppColors.ScanHint,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SideAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = AppColors.ScanTopText, modifier = Modifier.size(19.dp))
        }
        Text(label, color = AppColors.ScanHint, fontSize = 12.sp)
    }
}

@Composable
private fun Shutter(onClick: () -> Unit) {
    Box(
        Modifier
            .size(72.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFF6A60), AppColors.Laser),
                    radius = 90f,
                ),
                CircleShape,
            )
            .border(5.dp, AppColors.Laser.copy(alpha = 0.22f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.QrCodeScanner, "Scan now", tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
