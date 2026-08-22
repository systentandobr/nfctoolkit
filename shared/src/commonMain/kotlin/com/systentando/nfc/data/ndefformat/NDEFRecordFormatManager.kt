package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload

class NDEFRecordFormatManager {

    private val formatters: List<NDEFRecordFormatter> = listOf(
        TextRecordFormatter(),
        UriRecordFormatter(),
        WiFiRecordFormatter()
    )

    fun format(payload: WritePayload): NDEFRecord {
        val formatter = formatters.firstOrNull { it.supports(payload) }
            ?: throw IllegalArgumentException("No formatter for payload: $payload")
        return formatter.format(payload)
    }
}
