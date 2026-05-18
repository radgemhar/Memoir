package com.example.memoir.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoirRepository @Inject constructor(
    private val memoirDao: MemoirDao
) {
    fun getActiveMemoirs(): Flow<List<Memoir>> = memoirDao.getActiveMemoirs()
    fun getArchivedMemoirs(): Flow<List<Memoir>> = memoirDao.getArchivedMemoirs()
    fun searchMemoirs(query: String): Flow<List<Memoir>> = memoirDao.searchMemoirs(query)
    
    suspend fun getMemoirById(id: String): Memoir? = memoirDao.getMemoirById(id)
    
    suspend fun addMemoir(memoir: Memoir) = memoirDao.insertMemoir(memoir)
    
    suspend fun updateMemoir(memoir: Memoir) = memoirDao.updateMemoir(memoir)
    
    suspend fun deleteMemoir(memoir: Memoir) = memoirDao.deleteMemoir(memoir)

    suspend fun archiveMemoir(memoir: Memoir) {
        memoirDao.updateMemoir(memoir.copy(isArchived = true, lastModified = System.currentTimeMillis()))
    }

    suspend fun unarchiveMemoir(memoir: Memoir) {
        memoirDao.updateMemoir(memoir.copy(isArchived = false, lastModified = System.currentTimeMillis()))
    }
    
    suspend fun togglePin(memoir: Memoir) {
        memoirDao.updateMemoir(memoir.copy(isPinned = !memoir.isPinned, lastModified = System.currentTimeMillis()))
    }
}
