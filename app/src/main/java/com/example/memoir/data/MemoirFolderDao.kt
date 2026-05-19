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
}
