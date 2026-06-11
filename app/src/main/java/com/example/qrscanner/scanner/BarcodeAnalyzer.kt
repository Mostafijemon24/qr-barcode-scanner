package com.example.qrscanner.scanner

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/** A decoded code handed back from either the live camera or a gallery image. */
data class RawDetection(
    val value: String,
    val formatName: String,
    val isLinear: Boolean,
)

/** CameraX analyzer that runs ML Kit on every frame and reports the first code found. */
class BarcodeAnalyzer(
    private val onDetected: (RawDetection) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { !it.rawValue.isNullOrEmpty() }?.let { bc ->
                    onDetected(
                        RawDetection(
                            value = bc.rawValue!!,
                            formatName = BarcodeFormats.name(bc.format),
                            isLinear = BarcodeFormats.isLinear(bc.format),
                        )
                    )
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

/** Decodes barcodes from a still image (gallery picker). */
object GalleryScanner {
    fun scan(
        context: Context,
        uri: Uri,
        onDone: (RawDetection?) -> Unit,
    ) {
        val input = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            onDone(null)
            return
        }
        val scanner = BarcodeScanning.getClient()
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val bc = barcodes.firstOrNull { !it.rawValue.isNullOrEmpty() }
                onDone(
                    bc?.let {
                        RawDetection(it.rawValue!!, BarcodeFormats.name(it.format), BarcodeFormats.isLinear(it.format))
                    }
                )
            }
            .addOnFailureListener { onDone(null) }
            .addOnCompleteListener { scanner.close() }
    }
}

/** Maps ML Kit format ints to human names and linear/2D classification. */
object BarcodeFormats {
    fun isLinear(format: Int): Boolean = when (format) {
        Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93,
        Barcode.FORMAT_CODABAR, Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E -> true
        else -> false
    }

    fun name(format: Int): String = when (format) {
        Barcode.FORMAT_QR_CODE -> "QR code"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_CODE_128 -> "CODE-128"
        Barcode.FORMAT_CODE_39 -> "CODE-39"
        Barcode.FORMAT_CODE_93 -> "CODE-93"
        Barcode.FORMAT_CODABAR -> "Codabar"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        else -> "Barcode"
    }
}
