package com.example.memoir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoirFolderDao {
    @Query("SELECT * FROM memoir_folders ORDER BY CASE WHEN name = 'All' THEN 0 ELSE 1 END, createdAt ASC")
    fun getFolders(): Flow<List<MemoirFolder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: MemoirFolder)

    @Query("DELETE FROM memoir_folders WHERE name = :name")
    suspend fun deleteFolder(name: String)

    @Query("UPDATE memoir_folders SET name = :newName WHERE name = :oldName")
    suspend fun renameFolder(oldName: String, newName: String)

    @Query("UPDATE memoirs SET deletedAt = :deletedAt, lastModified = :deletedAt, isArchived = 0, folderName = 'All' WHERE folderName = :folderName AND deletedAt IS NULL")
    suspend fun softDeleteMemoirsInFolder(folderName: String, deletedAt: Long)

    @Query("UPDATE memoirs SET folderName = :newName WHERE folderName = :oldName")
    suspend fun renameMemoirsFolder(oldName: String, newName: String)
}
