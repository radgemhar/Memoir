package com.example.memoir.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.Milestone
import com.example.memoir.data.MilestoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class MilestoneFilter(val label: String) {
    ALL("All"),
    TODO("To Do"),
    ACCOMPLISHED("Accomplished")
}

data class MilestoneWithTasks(
    val milestone: Milestone,
    val tasks: List<Milestone> = emptyList()
)

data class TaskItemInput(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@HiltViewModel
class MilestonesViewModel @Inject constructor(
    private val repository: MilestoneRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(MilestoneFilter.ALL)
    val filter: StateFlow<MilestoneFilter> = _filter

    private val _refreshRequests = MutableStateFlow(0)

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    private val _editingMilestone = MutableStateFlow<MilestoneWithTasks?>(null)
    val editingMilestone: StateFlow<MilestoneWithTasks?> = _editingMilestone

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val milestones: StateFlow<List<MilestoneWithTasks>> = combine(
        _searchQuery,
        _filter,
        _refreshRequests
    ) { query, filter, refreshCount ->
        Triple(query.trim(), filter, refreshCount)
    }.flatMapLatest { (query, filter, _) ->
        repository.getAllMilestones().map { allList ->
            val rootMilestones = allList.filter { it.parentId == null }
            val childrenMap = allList.filter { it.parentId != null }.groupBy { it.parentId }

            rootMilestones.map { root ->
                val children = (childrenMap[root.id] ?: emptyList()).sortedBy { it.createdAt }
                MilestoneWithTasks(root, children)
            }.filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.milestone.title.contains(query, ignoreCase = true) ||
                        item.tasks.any { it.title.contains(query, ignoreCase = true) }
                val matchesFilter = when (filter) {
                    MilestoneFilter.ALL -> true
                    MilestoneFilter.TODO -> !item.milestone.isCompleted
                    MilestoneFilter.ACCOMPLISHED -> item.milestone.isCompleted
                }
                matchesQuery && matchesFilter
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refresh()
    }

    fun setFilter(filter: MilestoneFilter) {
        _filter.value = filter
    }

    fun refresh() {
        _refreshRequests.value += 1
    }

    fun openAddMilestone() {
        _showAddDialog.value = true
    }

    fun closeAddMilestone() {
        _showAddDialog.value = false
    }

    fun openEditMilestone(milestone: MilestoneWithTasks) {
        _editingMilestone.value = milestone
    }

    fun closeEditMilestone() {
        _editingMilestone.value = null
    }

    fun toggleMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.toggleMilestoneCompletion(milestone)
        }
    }

    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
        }
    }

    fun saveMilestone(
        id: String?,
        title: String,
        items: List<TaskItemInput>,
        deletedItemIds: List<String>,
        createdAt: Long
    ) {
        val validItems = items.filter { it.text.isNotBlank() }
        if (title.isBlank() && validItems.isEmpty()) return
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val parentId = id ?: UUID.randomUUID().toString()
                val allCompleted = validItems.isNotEmpty() && validItems.all { it.isCompleted }

                // Upsert parent
                repository.addMilestone(
                    Milestone(
                        id = parentId,
                        title = title.trim(),
                        isCompleted = allCompleted,
                        parentId = null,
                        createdAt = createdAt
                    )
                )

                // Upsert children
                validItems.forEach { item ->
                    repository.addMilestone(
                        Milestone(
                            id = item.id,
                            title = item.text.trim(),
                            isCompleted = item.isCompleted,
                            parentId = parentId,
                            createdAt = item.createdAt
                        )
                    )
                }

                // Delete removed children
                deletedItemIds.forEach { childId ->
                    repository.getMilestoneById(childId)?.let { child ->
                        repository.deleteMilestone(child)
                    }
                }
            } else {
                // Save each item as a single independent root milestone
                validItems.forEach { item ->
                    repository.addMilestone(
                        Milestone(
                            id = item.id,
                            title = item.text.trim(),
                            isCompleted = item.isCompleted,
                            parentId = null,
                            createdAt = item.createdAt
                        )
                    )
                }

                // If editing a parent milestone and removed title, delete parent milestone
                if (id != null) {
                    repository.getMilestoneById(id)?.let { parent ->
                        repository.deleteMilestone(parent)
                    }
                }

                // Delete removed children
                deletedItemIds.forEach { childId ->
                    repository.getMilestoneById(childId)?.let { child ->
                        repository.deleteMilestone(child)
                    }
                }
            }

            closeAddMilestone()
            closeEditMilestone()
        }
    }
}
