package com.example.memoir.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoirFolderRepository @Inject constructor(
    private val folderDao: MemoirFolderDao
) {
    private val _selectedFolder = MutableStateFlow(DEFAULT_FOLDER_NAME)
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    fun setSelectedFolder(name: String) {
        _selectedFolder.value = name
    }
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

    suspend fun deleteFolder(name: String) {
        if (name == DEFAULT_FOLDER_NAME) return
        folderDao.softDeleteMemoirsInFolder(name, System.currentTimeMillis())
        folderDao.deleteFolder(name)
    }

    suspend fun renameFolder(oldName: String, newName: String) {
        if (oldName == DEFAULT_FOLDER_NAME || newName.trim() == DEFAULT_FOLDER_NAME) return
        val cleanNewName = newName.trim()
        if (cleanNewName.isNotBlank() && cleanNewName != oldName) {
            folderDao.renameMemoirsFolder(oldName, cleanNewName)
            folderDao.renameFolder(oldName, cleanNewName)
        }
    }
}
