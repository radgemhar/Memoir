package com.example.memoir.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoirRepository @Inject constructor(
    private val memoirDao: MemoirDao
) {
    fun getActiveMemoirs(): Flow<List<Memoir>> = memoirDao.getActiveMemoirs()
    fun getActiveMemoirs(folderName: String): Flow<List<Memoir>> {
        return if (folderName == DEFAULT_FOLDER_NAME) {
            memoirDao.getActiveMemoirs()
        } else {
            memoirDao.getActiveMemoirsInFolder(folderName)
        }
    }
    fun getArchivedMemoirs(): Flow<List<Memoir>> = memoirDao.getArchivedMemoirs()
    fun getRecentlyDeletedMemoirs(): Flow<List<Memoir>> {
        return memoirDao.getRecentlyDeletedMemoirs(System.currentTimeMillis() - DELETED_RETENTION_MILLIS)
    }
    fun searchMemoirs(query: String): Flow<List<Memoir>> = memoirDao.searchMemoirs(query)
    fun searchMemoirs(query: String, folderName: String): Flow<List<Memoir>> {
        return if (folderName == DEFAULT_FOLDER_NAME) {
            memoirDao.searchMemoirs(query)
        } else {
            memoirDao.searchMemoirsInFolder(query, folderName)
        }
    }
    
    suspend fun getMemoirById(id: String): Memoir? = memoirDao.getMemoirById(id)
    
    suspend fun addMemoir(memoir: Memoir) = memoirDao.insertMemoir(memoir)
    
    suspend fun updateMemoir(memoir: Memoir) = memoirDao.updateMemoir(memoir)
    
    suspend fun moveToRecentlyDeleted(memoir: Memoir) {
        memoirDao.updateMemoir(
            memoir.copy(
                isArchived = false,
                deletedAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )
        )
    }

    suspend fun restoreMemoir(memoir: Memoir) {
        memoirDao.updateMemoir(
            memoir.copy(
                deletedAt = null,
                lastModified = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMemoirPermanently(memoir: Memoir) = memoirDao.deleteMemoirById(memoir.id)

    suspend fun deleteAllRecentlyDeleted() = memoirDao.deleteAllRecentlyDeleted()

    suspend fun purgeExpiredRecentlyDeleted() {
        memoirDao.purgeDeletedBefore(System.currentTimeMillis() - DELETED_RETENTION_MILLIS)
    }

    suspend fun archiveMemoir(memoir: Memoir) {
        memoirDao.updateMemoir(
            memoir.copy(
                isArchived = true,
                deletedAt = null,
                lastModified = System.currentTimeMillis()
            )
        )
    }

    suspend fun unarchiveMemoir(memoir: Memoir) {
        memoirDao.updateMemoir(memoir.copy(isArchived = false, lastModified = System.currentTimeMillis()))
    }

    suspend fun moveToFolder(memoir: Memoir, folderName: String) {
        memoirDao.updateMemoir(
            memoir.copy(
                folderName = folderName,
                lastModified = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun togglePin(memoir: Memoir) {
        memoirDao.updateMemoir(memoir.copy(isPinned = !memoir.isPinned, lastModified = System.currentTimeMillis()))
    }
}
