package com.example.qrscanner.scanner

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * Live camera viewfinder backed by CameraX. Continuously feeds frames to
 * [BarcodeAnalyzer]; detections are suppressed while [isPaused] (e.g. when the
 * result sheet is open).
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    torchOn: Boolean,
    zoomRatio: Float,
    isPaused: Boolean,
    onDetected: (RawDetection) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraRef = remember { mutableStateOf<Camera?>(null) }

    // Always read the freshest values from inside the long-lived analyzer.
    val paused = rememberUpdatedState(isPaused)
    val detected = rememberUpdatedState(onDetected)

    DisposableEffect(lensFacing, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor, BarcodeAnalyzer { d ->
                        if (!paused.value) detected.value(d)
                    })
                }
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                provider.unbindAll()
                cameraRef.value = provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (_: Exception) {
                // Lens unavailable on this device — ignore and keep the previous binding.
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Exception) {
            }
            cameraRef.value = null
        }
    }

    LaunchedEffect(torchOn, cameraRef.value) {
        cameraRef.value?.let { cam ->
            if (cam.cameraInfo.hasFlashUnit()) cam.cameraControl.enableTorch(torchOn)
        }
    }

    LaunchedEffect(zoomRatio, cameraRef.value) {
        cameraRef.value?.cameraControl?.setZoomRatio(zoomRatio)
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
