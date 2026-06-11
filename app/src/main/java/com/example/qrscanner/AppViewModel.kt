package com.example.qrscanner

import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.qrscanner.model.ClassifiedScan
import com.example.qrscanner.model.ScanEntry
import com.example.qrscanner.scanner.RawDetection
import com.example.qrscanner.util.ContentClassifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Which codes the active type-chip lets through. */
enum class ScanFilter(val label: String) {
    QR("QR code"),
    BARCODE("Barcode"),
    ALL("All types");

    fun accepts(d: RawDetection): Boolean = when (this) {
        QR -> !d.isLinear        // 2D codes (QR & friends)
        BARCODE -> d.isLinear    // 1D linear barcodes
        ALL -> true
    }
}

/** Holds all app state: camera controls, the active result, and scan history. */
class AppViewModel : ViewModel() {

    val history = mutableStateListOf<ScanEntry>()

    var currentResult by mutableStateOf<ClassifiedScan?>(null)
        private set
    var filter by mutableStateOf(ScanFilter.QR)
    var torchOn by mutableStateOf(false)
        private set
    var lensFacing by mutableStateOf(CameraSelector.LENS_FACING_BACK)
        private set
    var zoomRatio by mutableStateOf(1f)
        private set

    private var nextId = 1000L
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        seedHistory()
    }

    /** True while a result sheet is showing → camera should stop reporting. */
    val isPaused: Boolean get() = currentResult != null

    fun onDetected(detection: RawDetection) {
        if (currentResult != null) return          // already showing a result
        if (!filter.accepts(detection)) return
        val classified = ContentClassifier.classify(detection.value, detection.formatName, detection.isLinear)
        currentResult = classified
        history.add(0, ScanEntry(nextId++, classified, timeFmt.format(Date()), "Today"))
    }

    /** A code picked from the gallery should show even if it is not the active filter. */
    fun onGalleryDetection(detection: RawDetection) {
        val classified = ContentClassifier.classify(detection.value, detection.formatName, detection.isLinear)
        currentResult = classified
        history.add(0, ScanEntry(nextId++, classified, timeFmt.format(Date()), "Today"))
    }

    fun dismissResult() {
        currentResult = null
    }

    fun toggleTorch() {
        torchOn = !torchOn
    }

    fun flipLens() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        torchOn = false // front cameras have no torch
        zoomRatio = 1f
    }

    fun cycleZoom() {
        zoomRatio = when {
            zoomRatio < 1.9f -> 2f
            zoomRatio < 3.9f -> 4f
            else -> 1f
        }
    }

    private fun seedHistory() {
        val seeds = listOf(
            Triple("https://example.com/offers", "10:42", "Today"),
            Triple("WIFI:S:Office_WiFi_5G;T:WPA;P:welcome123;;", "09:15", "Today"),
            Triple("8 941100 770023", "18:30", "Yesterday"),
            Triple("Event ticket — Hall 3, Seat B12", "14:05", "Yesterday"),
            Triple("https://pay.example.com/confirm", "11:20", "Yesterday"),
        )
        seeds.forEach { (raw, time, day) ->
            val isBarcode = raw.firstOrNull()?.isDigit() == true && raw.any { it == ' ' }
            val format = if (isBarcode) "EAN-13" else "QR code"
            val classified = ContentClassifier.classify(raw, format, isBarcode)
            history.add(ScanEntry(nextId++, classified, time, day))
        }
    }
}
