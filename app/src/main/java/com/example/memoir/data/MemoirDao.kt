package com.example.memoir.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoirDao {
    @Query("SELECT * FROM memoirs WHERE isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun getActiveMemoirs(): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedMemoirs(): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun searchMemoirs(query: String): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE id = :id")
    suspend fun getMemoirById(id: String): Memoir?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoir(memoir: Memoir)

    @Update
    suspend fun updateMemoir(memoir: Memoir)

    @Delete
    suspend fun deleteMemoir(memoir: Memoir)
}
