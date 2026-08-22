package com.systentando.nfc.data.ndefformat

import com.systentando.nfc.domain.model.NDEFRecord

object NDEFParser {

    fun parse(ndefBytes: ByteArray): List<NDEFRecord> {
        if (ndefBytes.isEmpty()) return emptyList()

        val records = mutableListOf<NDEFRecord>()
        var index = 0

        while (index < ndefBytes.size) {
            val header = ndefBytes[index].toInt()
            val tnf = (header and 0x07).toShort()
            val idLengthPresent = (header and 0x08) != 0
            val shortRecord = (header and 0x10) != 0
            val messageEnd = (header and 0x40) != 0

            index++
            if (index >= ndefBytes.size) break

            val typeLength = ndefBytes[index].toInt() and 0xFF
            index++

            val payloadLength = if (shortRecord) {
                (ndefBytes[index].toInt() and 0xFF).also { index++ }
            } else {
                ((ndefBytes[index].toInt() and 0xFF) shl 24) or
                ((ndefBytes[index + 1].toInt() and 0xFF) shl 16) or
                ((ndefBytes[index + 2].toInt() and 0xFF) shl 8) or
                (ndefBytes[index + 3].toInt() and 0xFF)
                    .also { index += 4 }
            }

            val idLength = if (idLengthPresent) {
                (ndefBytes[index].toInt() and 0xFF).also { index++ }
            } else 0

            val typeBytes = ndefBytes.copyOfRange(index, index + typeLength)
            index += typeLength

            val idBytes = if (idLength > 0) ndefBytes.copyOfRange(index, index + idLength) else ByteArray(0)
            index += idLength

            val payloadBytes = ndefBytes.copyOfRange(index, index + payloadLength)
            index += payloadLength

            records.add(
                decodeRecord(
                    tnf = tnf,
                    type = typeBytes.decodeToString(),
                    id = idBytes.decodeToString(),
                    payload = payloadBytes
                )
            )

            if (messageEnd) break
        }

        return records
    }

    private fun decodeRecord(
        tnf: Short,
        type: String,
        id: String,
        payload: ByteArray
    ): NDEFRecord {
        return when {
            tnf == 0x01.toShort() && type == "T" -> {
                val languageLength = payload[0].toInt() and 0x3F
                val language = payload.copyOfRange(1, 1 + languageLength).decodeToString()
                val text = payload.copyOfRange(1 + languageLength, payload.size).decodeToString()
                NDEFRecord.Text(id, text, language)
            }
            tnf == 0x01.toShort() && type == "U" -> {
                val prefixIndex = if (payload.isNotEmpty()) payload[0].toInt() and 0xFF else 0
                val prefixes = listOf(
                    "", "http://www.", "https://www.", "http://", "https://",
                    "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.",
                    "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://",
                    "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:",
                    "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://",
                    "tcpobex://", "irdaobex://", "file://", "urn:epc:id:",
                    "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:",
                    "urn:nfc:", "geo:"
                )
                val prefix = prefixes.getOrElse(prefixIndex) { "" }
                val uri = prefix + payload.copyOfRange(1, payload.size).decodeToString()
                NDEFRecord.Uri(id, uri)
            }
            tnf == 0x02.toShort() -> NDEFRecord.Mime(id, type, payload.decodeToString())
            tnf == 0x04.toShort() -> NDEFRecord.External(id, type, payload)
            else -> NDEFRecord.Unknown(id, tnf, type, payload)
        }
    }
}
