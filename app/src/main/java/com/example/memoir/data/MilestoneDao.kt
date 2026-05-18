package com.example.memoir.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY createdAt DESC")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE parentId IS NULL ORDER BY createdAt DESC")
    fun getRootMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE parentId = :parentId ORDER BY createdAt DESC")
    fun getSubMilestones(parentId: String): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun getMilestoneById(id: String): Milestone?

    @Query("SELECT * FROM milestones WHERE parentId = :parentId")
    suspend fun getChildrenOf(parentId: String): List<Milestone>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(milestone: Milestone)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)

    @Query("SELECT * FROM milestones WHERE parentId = :parentId")
    suspend fun getSubMilestonesSync(parentId: String): List<Milestone>
}
