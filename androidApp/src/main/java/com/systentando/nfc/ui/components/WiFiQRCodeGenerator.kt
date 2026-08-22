package com.systentando.nfc.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object WiFiQRCodeGenerator {

    fun generate(
        ssid: String,
        password: String,
        security: String = "WPA",
        size: Int = 512
    ): Bitmap? {
        val wifiString = buildWifiString(ssid, password, security)
        return try {
            val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val bitMatrix = QRCodeWriter().encode(wifiString, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun buildWifiString(ssid: String, password: String, security: String): String {
        val escapedSsid = ssid.replace("\\", "\\\\").replace(";", "\\;").replace("\"", "\\\"")
        val escapedPassword = password.replace("\\", "\\\\").replace(";", "\\;").replace("\"", "\\\"")
        return "WIFI:S:$escapedSsid;T:$security;P:$escapedPassword;;"
    }
}
