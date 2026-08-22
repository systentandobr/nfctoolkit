package com.systentando.nfc.domain.model

sealed class NDEFRecord(
    open val id: String,
    open val tnf: Short,
    open val type: String
) {
    data class Text(
        override val id: String,
        val text: String,
        val language: String = "en"
    ) : NDEFRecord(id, 0x01, "T")

    data class Uri(
        override val id: String,
        val uri: String
    ) : NDEFRecord(id, 0x01, "U")

    data class Mime(
        override val id: String,
        override val type: String,
        val payload: String
    ) : NDEFRecord(id, 0x02, type)

    data class External(
        override val id: String,
        override val type: String,
        val payload: ByteArray
    ) : NDEFRecord(id, 0x04, type)

    data class Unknown(
        override val id: String,
        override val tnf: Short,
        override val type: String,
        val payload: ByteArray
    ) : NDEFRecord(id, tnf, type)
}
