package com.example.qrscanner.model

/** High-level category a scanned or created payload falls into. */
enum class ScanKind { URL, WIFI, PHONE, EMAIL, GEO, SMS, TEXT, PRODUCT }

/** Contextual primary button shown on the result sheet. */
enum class PrimaryAction(val label: String) {
    OPEN_LINK("Open link"),
    CALL("Call"),
    EMAIL("Email"),
    OPEN_MAP("Open map"),
    SEND_SMS("Send SMS"),
    COPY_PASSWORD("Copy password"),
    SEARCH_WEB("Search web"),
    NONE("—"),
}

/** Result of decoding + classifying a single code. */
data class ClassifiedScan(
    val raw: String,
    val kind: ScanKind,
    val isBarcode: Boolean,   // true for 1D linear formats (EAN, UPC, CODE-128 …)
    val formatName: String,   // "QR code", "EAN-13", "CODE-128" …
    val heading: String,      // "Website link", "Wi-Fi network" …
    val payloadLabel: String, // "URL", "TEXT", "WIFI", "TEL" …
    val primaryAction: PrimaryAction,
) {
    /** Short title used in the history list. */
    val historyTitle: String
        get() = when (kind) {
            ScanKind.URL -> raw.removePrefix("https://").removePrefix("http://").trimEnd('/')
            ScanKind.WIFI -> WifiParser.ssid(raw) ?: "Wi-Fi network"
            else -> raw
        }

    /** Secondary line in the history list, e.g. "QR · Website link". */
    val historySubtitle: String
        get() = buildString {
            append(if (isBarcode) "Barcode" else "QR")
            append(" · ")
            if (isBarcode) {
                append(formatName)
                append(" · ")
            }
            append(heading)
        }
}

/** A row in the History screen. */
data class ScanEntry(
    val id: Long,
    val scan: ClassifiedScan,
    val time: String,       // "10:42"
    val dayLabel: String,   // "Today", "Yesterday" …
)

/** Minimal WIFI: payload parser. */
object WifiParser {
    fun ssid(raw: String): String? = field(raw, "S")
    fun password(raw: String): String? = field(raw, "P")

    private fun field(raw: String, key: String): String? {
        if (!raw.startsWith("WIFI:", ignoreCase = true)) return null
        // fields look like  S:My Net;  — split on unescaped ';'
        val regex = Regex("(?:^|;)$key:((?:[^;\\\\]|\\\\.)*)", RegexOption.IGNORE_CASE)
        return regex.find(raw)?.groupValues?.get(1)
            ?.replace("\\;", ";")?.replace("\\:", ":")?.replace("\\\\", "\\")
            ?.takeIf { it.isNotEmpty() }
    }
}
