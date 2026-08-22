package com.systentando.nfc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TagDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
}
