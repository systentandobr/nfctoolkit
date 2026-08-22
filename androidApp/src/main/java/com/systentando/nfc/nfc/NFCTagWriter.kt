package com.systentando.nfc.nfc

import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.systentando.nfc.data.ndefformat.NDEFRecordFormatManager
import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.WritePayload
import java.nio.charset.Charset
import javax.inject.Inject

class NFCTagWriter @Inject constructor() {

    private val formatManager = NDEFRecordFormatManager()

    fun write(tag: Tag, payload: WritePayload): Boolean {
        val record = formatManager.format(payload)
        val ndefRecord = record.toAndroidNdefRecord()
        val message = NdefMessage(arrayOf(ndefRecord))

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return try {
                ndef.connect()
                ndef.writeNdefMessage(message)
                ndef.close()
                true
            } catch (e: Exception) {
                false
            }
        }

        val formatable = NdefFormatable.get(tag)
        if (formatable != null) {
            return try {
                formatable.connect()
                formatable.format(message)
                formatable.close()
                true
            } catch (e: Exception) {
                false
            }
        }

        return false
    }

    fun erase(tag: Tag): Boolean {
        val emptyRecord = NdefRecord.createTextRecord("en", "")
        val message = NdefMessage(arrayOf(emptyRecord))
        return try {
            val ndef = Ndef.get(tag) ?: return false
            ndef.connect()
            ndef.writeNdefMessage(message)
            ndef.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}

private fun NDEFRecord.toAndroidNdefRecord(): NdefRecord {
    return when (this) {
        is NDEFRecord.Text -> {
            val languageBytes = language.encodeToByteArray()
            val textBytes = text.encodeToByteArray()
            val payload = byteArrayOf((0x80 or languageBytes.size).toByte()) + languageBytes + textBytes
            NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, id.encodeToByteArray(), payload)
        }
        is NDEFRecord.Uri -> {
            NdefRecord.createUri(Uri.parse(uri))
        }
        is NDEFRecord.Mime -> {
            NdefRecord.createMime(type, payload.encodeToByteArray())
        }
        is NDEFRecord.External -> {
            NdefRecord.createExternal(
                type.substringBeforeLast(":"),
                type.substringAfterLast(":").removePrefix("/"),
                payload
            )
        }
        is NDEFRecord.Unknown -> {
            NdefRecord(tnf.toShort(), type.encodeToByteArray(), id.encodeToByteArray(), payload)
        }
    }
}
