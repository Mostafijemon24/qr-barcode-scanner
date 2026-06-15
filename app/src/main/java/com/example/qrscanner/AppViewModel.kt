package com.example.qrscanner

import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.qrscanner.model.ClassifiedScan
import com.example.qrscanner.model.ScanEntry
import com.example.qrscanner.scanner.RawDetection
import com.example.qrscanner.util.ContentClassifier
import com.example.qrscanner.util.ScanHistoryStore

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
class AppViewModel(application: Application) : AndroidViewModel(application) {

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

    private val historyStore = ScanHistoryStore(application)
    private var nextId = 1L

    init {
        val loaded = historyStore.load()
        history.addAll(loaded)
        nextId = (loaded.maxOfOrNull { it.id } ?: 0L) + 1
    }

    /** True while a result sheet is showing → camera should stop reporting. */
    val isPaused: Boolean get() = currentResult != null

    fun onDetected(detection: RawDetection) {
        if (currentResult != null) return          // already showing a result
        if (!filter.accepts(detection)) return
        val classified = ContentClassifier.classify(detection.value, detection.formatName, detection.isLinear)
        currentResult = classified
        addToHistory(classified)
    }

    /** A code picked from the gallery should show even if it is not the active filter. */
    fun onGalleryDetection(detection: RawDetection) {
        val classified = ContentClassifier.classify(detection.value, detection.formatName, detection.isLinear)
        currentResult = classified
        addToHistory(classified)
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

    private fun addToHistory(classified: ClassifiedScan) {
        val entry = ScanEntry(
            id = nextId++,
            scan = classified,
            timestampMillis = System.currentTimeMillis(),
        )
        history.add(0, entry)
        historyStore.save(history)
    }
}
