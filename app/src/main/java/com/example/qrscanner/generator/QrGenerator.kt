package com.example.qrscanner.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Renders QR codes to bitmaps using ZXing. */
object QrGenerator {

    private const val MODULE_DARK = 0xFF0B1118.toInt() // --ink
    private const val MODULE_LIGHT = 0xFFFFFFFF.toInt()
    private const val LOGO_SCALE = 0.22f
    private const val LOGO_PAD_SCALE = 0.08f

    fun generate(content: String, sizePx: Int, logo: Bitmap? = null): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to if (logo != null) {
                    ErrorCorrectionLevel.H
                } else {
                    ErrorCorrectionLevel.M
                },
            )
            val matrix = MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix.get(x, y)) MODULE_DARK else MODULE_LIGHT
                }
            }
            val qr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
            if (logo != null) overlayLogo(qr, logo)
            qr
        } catch (e: Exception) {
            null
        }
    }

    private fun overlayLogo(qr: Bitmap, logo: Bitmap) {
        val size = qr.width
        val logoSize = (size * LOGO_SCALE).toInt().coerceAtLeast(1)
        val pad = (size * LOGO_PAD_SCALE).toInt()
        val total = logoSize + pad * 2
        val left = (size - total) / 2f
        val top = (size - total) / 2f

        val canvas = Canvas(qr)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MODULE_LIGHT }
        val rect = RectF(left, top, left + total, top + total)
        canvas.drawRoundRect(rect, total * 0.12f, total * 0.12f, bgPaint)

        val cropped = squareCrop(logo)
        val scaled = Bitmap.createScaledBitmap(cropped, logoSize, logoSize, true)
        if (cropped !== logo) cropped.recycle()
        canvas.drawBitmap(scaled, left + pad, top + pad, null)
        if (scaled !== logo) scaled.recycle()
    }

    private fun squareCrop(source: Bitmap): Bitmap {
        val dim = minOf(source.width, source.height)
        if (source.width == dim && source.height == dim) return source
        val x = (source.width - dim) / 2
        val y = (source.height - dim) / 2
        return Bitmap.createBitmap(source, x, y, dim, dim)
    }
}
