package com.systentando.nfc.nfc

import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import com.systentando.nfc.data.ndefformat.NDEFParser
import com.systentando.nfc.domain.model.NFCTag
import javax.inject.Inject

class NFCTagReader @Inject constructor() {

    fun read(tag: Tag): NFCTag {
        val uid = tag.id?.toHex() ?: ""
        val techList = tag.techList.map { it.substringAfterLast('.') }
        val ndef = Ndef.get(tag)

        return if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            val records = ndefMessage?.records?.flatMap { record ->
                parseRecord(record)
            } ?: emptyList()
            val type = ndef.type
            val size = ndef.maxSize
            ndef.close()

            val usedBytes = ndefMessage?.records?.sumOf { recordSize(it) } ?: 0

            NFCTag(
                uid = uid,
                chipType = type ?: techList.firstOrNull() ?: "Unknown",
                technology = techList.firstOrNull() ?: "NFC",
                sizeBytes = size,
                usedBytes = usedBytes,
                isNDEF = true,
                records = records,
                readAt = System.currentTimeMillis()
            )
        } else {
            NFCTag(
                uid = uid,
                chipType = techList.firstOrNull() ?: "Unknown",
                technology = techList.firstOrNull() ?: "NFC",
                sizeBytes = 0,
                usedBytes = 0,
                isNDEF = false,
                rawHex = tag.id?.toHex(),
                readAt = System.currentTimeMillis()
            )
        }
    }

    private fun parseRecord(record: NdefRecord): List<com.systentando.nfc.domain.model.NDEFRecord> {
        val bytes = record.toNdefBytes()
        return NDEFParser.parse(bytes)
    }

    private fun recordSize(record: NdefRecord): Int {
        return record.toNdefBytes().size
    }

    private fun ByteArray.toHex(): String {
        return joinToString(":") { "%02X".format(it) }
    }
}

private fun NdefRecord.toNdefBytes(): ByteArray {
    val tnf = this.tnf.toInt() and 0x07
    val type = this.type ?: ByteArray(0)
    val id = this.id ?: ByteArray(0)
    val payload = this.payload ?: ByteArray(0)

    val header = tnf
    var actualHeader = header
    val typeLength = type.size
    val payloadLength = payload.size
    val idLength = id.size

    if (idLength > 0) actualHeader = actualHeader or 0x08
    val shortRecord = payloadLength < 256
    if (shortRecord) actualHeader = actualHeader or 0x10

    val result = mutableListOf<Byte>()
    result.add(actualHeader.toByte())
    result.add(typeLength.toByte())
    if (shortRecord) {
        result.add(payloadLength.toByte())
    } else {
        result.add((payloadLength shr 24).toByte())
        result.add((payloadLength shr 16).toByte())
        result.add((payloadLength shr 8).toByte())
        result.add(payloadLength.toByte())
    }
    if (idLength > 0) {
        result.add(idLength.toByte())
    }
    result.addAll(type.toList())
    result.addAll(id.toList())
    result.addAll(payload.toList())

    return result.toByteArray()
}
