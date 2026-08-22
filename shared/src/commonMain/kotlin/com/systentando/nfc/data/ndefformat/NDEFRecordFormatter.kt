package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload

interface NDEFRecordFormatter {
    fun supports(payload: WritePayload): Boolean
    fun format(payload: WritePayload): NDEFRecord
}
