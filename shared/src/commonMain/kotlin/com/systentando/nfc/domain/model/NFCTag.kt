package com.systentando.nfc.domain.model

data class NFCTag(
    val uid: String,
    val chipType: String,
    val technology: String,
    val sizeBytes: Int,
    val usedBytes: Int,
    val isNDEF: Boolean,
    val records: List<NDEFRecord> = emptyList(),
    val rawHex: String? = null,
    val readAt: Long,
    val nickname: String? = null
)
