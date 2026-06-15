package com.example.qrscanner.util

import android.content.Context
import com.example.qrscanner.model.ClassifiedScan
import com.example.qrscanner.model.PrimaryAction
import com.example.qrscanner.model.ScanEntry
import com.example.qrscanner.model.ScanKind
import org.json.JSONArray
import org.json.JSONObject

/** Rolling window for persisted scan history. */
const val HISTORY_RETENTION_MS = 7L * 24 * 60 * 60 * 1000

/**
 * Persists scan history in SharedPreferences as a JSON array.
 * Entries older than [HISTORY_RETENTION_MS] are dropped on load and save.
 */
class ScanHistoryStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<ScanEntry> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val cutoff = System.currentTimeMillis() - HISTORY_RETENTION_MS
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val entry = parseEntry(obj) ?: continue
                    if (entry.timestampMillis >= cutoff) add(entry)
                }
            }.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(entries: List<ScanEntry>) {
        val cutoff = System.currentTimeMillis() - HISTORY_RETENTION_MS
        val kept = entries
            .filter { it.timestampMillis >= cutoff }
            .sortedByDescending { it.timestampMillis }
        val arr = JSONArray()
        kept.forEach { arr.put(serializeEntry(it)) }
        prefs.edit().putString(KEY_ENTRIES, arr.toString()).apply()
    }

    private fun serializeEntry(entry: ScanEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("timestampMillis", entry.timestampMillis)
        put("scan", JSONObject().apply {
            put("raw", entry.scan.raw)
            put("kind", entry.scan.kind.name)
            put("isBarcode", entry.scan.isBarcode)
            put("formatName", entry.scan.formatName)
            put("heading", entry.scan.heading)
            put("payloadLabel", entry.scan.payloadLabel)
            put("primaryAction", entry.scan.primaryAction.name)
        })
    }

    private fun parseEntry(obj: JSONObject): ScanEntry? = try {
        val scanObj = obj.getJSONObject("scan")
        ScanEntry(
            id = obj.getLong("id"),
            timestampMillis = obj.getLong("timestampMillis"),
            scan = ClassifiedScan(
                raw = scanObj.getString("raw"),
                kind = ScanKind.valueOf(scanObj.getString("kind")),
                isBarcode = scanObj.getBoolean("isBarcode"),
                formatName = scanObj.getString("formatName"),
                heading = scanObj.getString("heading"),
                payloadLabel = scanObj.getString("payloadLabel"),
                primaryAction = PrimaryAction.valueOf(scanObj.getString("primaryAction")),
            ),
        )
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val PREFS_NAME = "scan_history"
        private const val KEY_ENTRIES = "entries"
    }
}
