package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord

object NDEFRecordSerializer {

    fun serialize(records: List<NDEFRecord>): ByteArray {
        val bytes = mutableListOf<Byte>()

        records.forEachIndexed { index, record ->
            val isFirst = index == 0
            val isLast = index == records.size - 1
            val header = buildHeader(record, isFirst, isLast)
            bytes.addAll(header.toList())
        }

        return bytes.toByteArray()
    }

    private fun buildHeader(record: NDEFRecord, isFirst: Boolean, isLast: Boolean): ByteArray {
        var header = record.tnf.toByte()
        if (isFirst) header = (header.toInt() or 0x80).toByte()
        if (isLast) header = (header.toInt() or 0x40).toByte()

        val type = getTypeString(record)
        val payload = getPayload(record)

        val typeLength = type.size
        val payloadLength = payload.size

        val result = mutableListOf<Byte>()
        result.add(header)
        result.add(typeLength.toByte())

        if (payloadLength < 256) {
            result.add((0x10).toByte())
            result.add(payloadLength.toByte())
        } else {
            result.add((payloadLength shr 24).toByte())
            result.add((payloadLength shr 16).toByte())
            result.add((payloadLength shr 8).toByte())
            result.add(payloadLength.toByte())
        }

        result.addAll(type.toList())
        result.addAll(payload.toList())

        return result.toByteArray()
    }

    private fun getTypeString(record: NDEFRecord): ByteArray {
        return when (record) {
            is NDEFRecord.Text -> byteArrayOf('T'.code.toByte())
            is NDEFRecord.Uri -> byteArrayOf('U'.code.toByte())
            is NDEFRecord.Mime -> record.type.encodeToByteArray()
            is NDEFRecord.External -> record.type.encodeToByteArray()
            is NDEFRecord.Unknown -> record.type.encodeToByteArray()
        }
    }

    private fun getPayload(record: NDEFRecord): ByteArray {
        return when (record) {
            is NDEFRecord.Text -> {
                val langBytes = record.language.encodeToByteArray()
                byteArrayOf((0x80 or langBytes.size).toByte()) + langBytes + record.text.encodeToByteArray()
            }
            is NDEFRecord.Uri -> {
                val uri = record.uri
                val (prefix, remainder) = resolveUri(uri)
                byteArrayOf(prefix.toByte()) + remainder.encodeToByteArray()
            }
            is NDEFRecord.Mime -> record.payload.encodeToByteArray()
            is NDEFRecord.External -> record.payload
            is NDEFRecord.Unknown -> record.payload
        }
    }

    private fun resolveUri(uri: String): Pair<Int, String> {
        val prefixes = listOf(
            "", "http://www.", "https://www.", "http://", "https://",
            "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.",
            "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://",
            "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:",
            "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://",
            "tcpobex://", "irdaobex://", "file://", "urn:epc:id:",
            "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:",
            "urn:nfc:"
        )

        val prefixIndex = prefixes.indexOfLast { uri.startsWith(it) && it.isNotEmpty() }
        return if (prefixIndex >= 0) {
            prefixIndex to uri.removePrefix(prefixes[prefixIndex])
        } else {
            0 to uri
        }
    }
}
