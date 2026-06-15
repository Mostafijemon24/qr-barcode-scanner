package com.example.qrscanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.qrscanner.generator.QrGenerator
import com.example.qrscanner.ui.theme.AppColors
import com.example.qrscanner.util.BitmapLoader
import com.example.qrscanner.util.ImageSaver

private enum class CreateType(val label: String, val fieldLabel: String, val hint: String) {
    URL("Website link", "Link or text", "https://mywebsite.com"),
    TEXT("Plain text", "Link or text", "Enter any text"),
    WIFI("Wi-Fi", "Network name (SSID)", "MyNetwork"),
    PHONE("Phone number", "Phone number", "+8801XXXXXXXXX");

    fun encode(value: String): String = when (this) {
        URL, TEXT -> value
        WIFI -> "WIFI:T:WPA;S:$value;;"
        PHONE -> "tel:$value"
    }
}

@Composable
fun CreateScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var type by remember { mutableStateOf(CreateType.URL) }
    var value by remember { mutableStateOf("https://mywebsite.com") }
    var menuOpen by remember { mutableStateOf(false) }
    var logoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val loaded = BitmapLoader.fromUri(context, uri)
            if (loaded != null) logoBitmap = loaded
            else Toast.makeText(context, "Couldn't load image", Toast.LENGTH_SHORT).show()
        }
    }

    val qrBitmap = remember(type, value, logoBitmap) {
        QrGenerator.generate(type.encode(value), 512, logoBitmap)
    }

    fun saveNow() {
        val content = type.encode(value)
        val bmp = QrGenerator.generate(content, 1024, logoBitmap)
        if (bmp == null) {
            Toast.makeText(context, "Enter something to encode first", Toast.LENGTH_SHORT).show()
            return
        }
        val ok = ImageSaver.saveToGallery(context, bmp, "qr_${System.currentTimeMillis()}.png")
        Toast.makeText(
            context,
            if (ok) "Saved to gallery" else "Couldn't save image",
            Toast.LENGTH_SHORT,
        ).show()
    }

    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveNow()
        else Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
    }

    fun onDownload() {
        val needsLegacyPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        val hasPermission = !needsLegacyPermission || ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) saveNow()
        else storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    Column(
        modifier
            .fillMaxSize()
            .background(AppColors.Panel)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // page head
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 12.dp)) {
            Text("Create a QR code", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            Text(
                "Turn a link, text, or Wi-Fi network into your own code",
                fontSize = 13.sp,
                color = AppColors.TextDim,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 120.dp)) {
            // preview
            Box(
                Modifier
                    .padding(top = 6.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(182.dp)
                    .background(AppColors.Card, RoundedCornerShape(18.dp))
                    .border(1.dp, AppColors.Line, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR preview",
                        modifier = Modifier.size(138.dp),
                    )
                } else {
                    Text("Preview", color = AppColors.TextDim, fontSize = 13.sp)
                }
            }

            // content type
            Text("Content type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Text)
            Box(Modifier.padding(top = 6.dp, bottom = 14.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppColors.Card, RoundedCornerShape(12.dp))
                        .border(1.dp, AppColors.Line, RoundedCornerShape(12.dp))
                        .clickable { menuOpen = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(type.label, fontSize = 14.sp, color = AppColors.Text)
                    Icon(Icons.Rounded.ExpandMore, "Open", tint = AppColors.TextDim, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    CreateType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label) },
                            onClick = {
                                type = t
                                menuOpen = false
                                if (t == CreateType.WIFI && value.startsWith("http")) value = ""
                            },
                        )
                    }
                }
            }

            // value field
            Text(type.fieldLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Text)
            Box(Modifier.padding(top = 6.dp, bottom = 16.dp)) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    textStyle = TextStyle(color = AppColors.Text, fontSize = 14.sp),
                    cursorBrush = SolidColor(AppColors.Ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Card, RoundedCornerShape(12.dp))
                        .border(1.dp, AppColors.Line, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(type.hint, color = AppColors.TextDim, fontSize = 14.sp)
                            }
                            inner()
                        }
                    },
                )
            }

            // center logo (optional)
            Text(
                "Center logo (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Text,
            )
            if (logoBitmap != null) {
                Row(
                    Modifier
                        .padding(top = 6.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .background(AppColors.Card, RoundedCornerShape(12.dp))
                        .border(1.dp, AppColors.Line, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = logoBitmap!!.asImageBitmap(),
                            contentDescription = "Logo preview",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AppColors.Line, RoundedCornerShape(8.dp)),
                        )
                        Text(
                            "Logo added",
                            fontSize = 14.sp,
                            color = AppColors.Text,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Change",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Ink,
                            modifier = Modifier
                                .clickable { logoPicker.launch("image/*") }
                                .padding(4.dp),
                        )
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Remove logo",
                            tint = AppColors.TextDim,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { logoBitmap = null }
                                .padding(2.dp),
                        )
                    }
                }
            } else {
                Row(
                    Modifier
                        .padding(top = 6.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .background(AppColors.Card, RoundedCornerShape(12.dp))
                        .border(1.dp, AppColors.Line, RoundedCornerShape(12.dp))
                        .clickable { logoPicker.launch("image/*") }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = AppColors.TextDim,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        "Add logo from gallery",
                        fontSize = 14.sp,
                        color = AppColors.TextDim,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }

            // create button
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(AppColors.Ink, RoundedCornerShape(13.dp))
                    .clickable { onDownload() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Create & download code", color = AppColors.Card, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
