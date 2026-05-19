package com.example.memoir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoirDao {
    @Query("SELECT * FROM memoirs WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, createdAt DESC")
    fun getActiveMemoirs(): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE isArchived = 0 AND deletedAt IS NULL AND folderName = :folderName ORDER BY isPinned DESC, createdAt DESC")
    fun getActiveMemoirsInFolder(folderName: String): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE isArchived = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getArchivedMemoirs(): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE deletedAt IS NOT NULL AND deletedAt >= :cutoff ORDER BY deletedAt DESC")
    fun getRecentlyDeletedMemoirs(cutoff: Long): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') AND isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, createdAt DESC")
    fun searchMemoirs(query: String): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') AND isArchived = 0 AND deletedAt IS NULL AND folderName = :folderName ORDER BY isPinned DESC, createdAt DESC")
    fun searchMemoirsInFolder(query: String, folderName: String): Flow<List<Memoir>>

    @Query("SELECT * FROM memoirs WHERE id = :id")
    suspend fun getMemoirById(id: String): Memoir?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoir(memoir: Memoir)

    @Update
    suspend fun updateMemoir(memoir: Memoir)

    @Query("DELETE FROM memoirs WHERE id = :id")
    suspend fun deleteMemoirById(id: String)

    @Query("DELETE FROM memoirs WHERE deletedAt IS NOT NULL")
    suspend fun deleteAllRecentlyDeleted()

    @Query("DELETE FROM memoirs WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long)
}
