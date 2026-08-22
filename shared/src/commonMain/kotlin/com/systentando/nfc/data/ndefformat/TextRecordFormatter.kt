package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload

class TextRecordFormatter : NDEFRecordFormatter {
    override fun supports(payload: WritePayload): Boolean {
        return payload is WritePayload.Text
    }

    override fun format(payload: WritePayload): NDEFRecord {
        val textPayload = payload as WritePayload.Text
        return NDEFRecord.Text(
            id = "",
            text = textPayload.text,
            language = "pt-BR"
        )
    }
}
