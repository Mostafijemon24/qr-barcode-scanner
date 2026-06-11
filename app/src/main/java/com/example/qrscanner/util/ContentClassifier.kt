package com.example.qrscanner.util

import com.example.qrscanner.model.ClassifiedScan
import com.example.qrscanner.model.PrimaryAction
import com.example.qrscanner.model.ScanKind

/**
 * Turns a raw decoded string (+ its barcode format) into a [ClassifiedScan]
 * that drives both the result sheet and the history list.
 */
object ContentClassifier {

    fun classify(raw: String, formatName: String, isBarcode: Boolean): ClassifiedScan {
        val value = raw.trim()
        val lower = value.lowercase()

        // Linear/product barcodes are always treated as product codes.
        if (isBarcode) {
            return ClassifiedScan(
                raw = value,
                kind = ScanKind.PRODUCT,
                isBarcode = true,
                formatName = formatName,
                heading = "Product code",
                payloadLabel = "PRODUCT",
                primaryAction = PrimaryAction.SEARCH_WEB,
            )
        }

        return when {
            lower.startsWith("http://") || lower.startsWith("https://") -> ClassifiedScan(
                value, ScanKind.URL, false, formatName,
                "Website link", "URL", PrimaryAction.OPEN_LINK
            )

            lower.startsWith("wifi:") -> ClassifiedScan(
                value, ScanKind.WIFI, false, formatName,
                "Wi-Fi network", "WIFI", PrimaryAction.COPY_PASSWORD
            )

            lower.startsWith("tel:") -> ClassifiedScan(
                value, ScanKind.PHONE, false, formatName,
                "Phone number", "TEL", PrimaryAction.CALL
            )

            lower.startsWith("mailto:") || lower.startsWith("matmsg:") -> ClassifiedScan(
                value, ScanKind.EMAIL, false, formatName,
                "Email address", "EMAIL", PrimaryAction.EMAIL
            )

            lower.startsWith("smsto:") || lower.startsWith("sms:") -> ClassifiedScan(
                value, ScanKind.SMS, false, formatName,
                "SMS message", "SMS", PrimaryAction.SEND_SMS
            )

            lower.startsWith("geo:") -> ClassifiedScan(
                value, ScanKind.GEO, false, formatName,
                "Location", "GEO", PrimaryAction.OPEN_MAP
            )

            lower.startsWith("begin:vcard") || lower.startsWith("mecard:") -> ClassifiedScan(
                value, ScanKind.TEXT, false, formatName,
                "Contact card", "VCARD", PrimaryAction.NONE
            )

            looksLikeBareUrl(value) -> ClassifiedScan(
                value, ScanKind.URL, false, formatName,
                "Website link", "URL", PrimaryAction.OPEN_LINK
            )

            else -> ClassifiedScan(
                value, ScanKind.TEXT, false, formatName,
                "Plain text", "TEXT", PrimaryAction.SEARCH_WEB
            )
        }
    }

    /** Detects "example.com/foo" style links that lack an explicit scheme. */
    private fun looksLikeBareUrl(value: String): Boolean {
        if (value.contains(' ') || value.contains('\n')) return false
        return Regex("^[\\w-]+(\\.[\\w-]+)+(/\\S*)?$").matches(value) &&
            value.substringBefore('/').contains('.')
    }
}
