package com.systentando.nfc.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY readAt DESC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()
}
