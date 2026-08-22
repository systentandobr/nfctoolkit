package com.systentando.nfc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val chipType: String,
    val technology: String,
    val sizeBytes: Int,
    val usedBytes: Int,
    val isNDEF: Boolean,
    val recordsJson: String,
    val rawHex: String?,
    val readAt: Long,
    val nickname: String?
)
