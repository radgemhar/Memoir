package com.example.memoir.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoirFolderRepository @Inject constructor(
    private val folderDao: MemoirFolderDao
) {
    fun getFolders(): Flow<List<MemoirFolder>> = folderDao.getFolders()

    suspend fun ensureDefaultFolder() {
        folderDao.insertFolder(MemoirFolder(DEFAULT_FOLDER_NAME, createdAt = 0L))
    }

    suspend fun addFolder(name: String) {
        val cleanName = name.trim()
        if (cleanName.isNotBlank()) {
            folderDao.insertFolder(MemoirFolder(cleanName))
        }
    }
}
