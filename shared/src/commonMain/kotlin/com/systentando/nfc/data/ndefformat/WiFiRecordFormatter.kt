package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload

class WiFiRecordFormatter : NDEFRecordFormatter {

    override fun supports(payload: WritePayload): Boolean {
        return payload is WritePayload.WiFi
    }

    override fun format(payload: WritePayload): NDEFRecord {
        val wifi = payload as WritePayload.WiFi
        return NDEFRecord.External(
            id = "",
            type = WIFI_MIME_TYPE,
            payload = buildWifiPayload(wifi)
        )
    }

    private fun buildWifiPayload(wifi: WritePayload.WiFi): ByteArray {
        val ssid = wifi.ssid.encodeToByteArray()
        val key = wifi.password.encodeToByteArray()

        val ssidField = byteArrayOf(0x10, 0x0E) + ssid
        val authType = when (wifi.security) {
            WritePayload.WiFiSecurityType.OPEN -> byteArrayOf(0x10, 0x03, 0x00, 0x01, 0x00, 0x01)
            WritePayload.WiFiSecurityType.WEP -> byteArrayOf(0x10, 0x03, 0x00, 0x02, 0x00, 0x01, 0x00, 0x02)
            WritePayload.WiFiSecurityType.WPA_WPA2 -> byteArrayOf(0x10, 0x03, 0x00, 0x02, 0x00, 0x02, 0x00, 0x01)
        }
        val encType = byteArrayOf(0x10, 0x0F, 0x00, 0x01, 0x00, 0x02)
        val keyField = byteArrayOf(0x10, 0x27) + key

        val config = byteArrayOf(0x10, 0x45) +
            ssidField +
            authType +
            encType +
            keyField

        return byteArrayOf(0x10, 0x26) + config
    }

    companion object {
        const val WIFI_MIME_TYPE = "application/vnd.wfa.wsc"
    }
}
