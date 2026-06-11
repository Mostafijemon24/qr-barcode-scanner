package com.example.qrscanner

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrscanner.ui.components.BottomNav
import com.example.qrscanner.ui.components.Screen
import com.example.qrscanner.ui.screens.CreateScreen
import com.example.qrscanner.ui.screens.HistoryScreen
import com.example.qrscanner.ui.screens.ScanScreen
import com.example.qrscanner.ui.theme.QRScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRScannerTheme {
                val vm: AppViewModel = viewModel()
                var screen by rememberSaveable { mutableStateOf(Screen.SCAN) }

                // Dark scan screen wants light status-bar icons; the light screens want dark.
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view)
                        .isAppearanceLightStatusBars = screen != Screen.SCAN
                }

                Box(Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.SCAN -> ScanScreen(vm)
                        Screen.CREATE -> CreateScreen()
                        Screen.HISTORY -> HistoryScreen(vm)
                    }

                    BottomNav(
                        current = screen,
                        onSelect = {
                            if (it != Screen.SCAN) vm.dismissResult()
                            screen = it
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    )
                }
            }
        }
    }
}
