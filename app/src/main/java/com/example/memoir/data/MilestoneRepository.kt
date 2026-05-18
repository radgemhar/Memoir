package com.example.memoir.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneRepository @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    fun getAllMilestones(): Flow<List<Milestone>> = milestoneDao.getAllMilestones()
    fun getRootMilestones(): Flow<List<Milestone>> = milestoneDao.getRootMilestones()
    fun getSubMilestones(parentId: String): Flow<List<Milestone>> = milestoneDao.getSubMilestones(parentId)

    suspend fun getMilestoneById(id: String): Milestone? = milestoneDao.getMilestoneById(id)

    suspend fun addMilestone(milestone: Milestone) = milestoneDao.upsertMilestone(milestone)

    suspend fun deleteMilestone(milestone: Milestone) = milestoneDao.deleteMilestone(milestone)

    suspend fun toggleMilestoneCompletion(milestone: Milestone) {
        val newStatus = !milestone.isCompleted
        updateMilestoneWithHierarchy(milestone.copy(isCompleted = newStatus))
    }

    private suspend fun updateMilestoneWithHierarchy(milestone: Milestone) {
        milestoneDao.updateMilestone(milestone)
        updateDescendants(milestone.id, milestone.isCompleted)
        updateAncestors(milestone.parentId)
    }

    private suspend fun updateDescendants(parentId: String, completed: Boolean) {
        val children = milestoneDao.getSubMilestonesSync(parentId)
        for (child in children) {
            if (child.isCompleted != completed) {
                val updatedChild = child.copy(isCompleted = completed)
                milestoneDao.updateMilestone(updatedChild)
                updateDescendants(updatedChild.id, completed)
            }
        }
    }

    private suspend fun updateAncestors(parentId: String?) {
        if (parentId == null) return

        val parent = milestoneDao.getMilestoneById(parentId) ?: return
        val siblings = milestoneDao.getSubMilestonesSync(parentId)
        
        val allSiblingsCompleted = siblings.isNotEmpty() && siblings.all { it.isCompleted }
        
        if (parent.isCompleted != allSiblingsCompleted) {
            val updatedParent = parent.copy(isCompleted = allSiblingsCompleted)
            milestoneDao.updateMilestone(updatedParent)
            updateAncestors(updatedParent.parentId)
        }
    }
}
