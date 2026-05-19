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
    ACTIVE("Active"),
    COMPLETED("Done")
}

@HiltViewModel
class MilestonesViewModel @Inject constructor(
    private val repository: MilestoneRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(MilestoneFilter.ALL)
    val filter: StateFlow<MilestoneFilter> = _filter

    private val _refreshRequests = MutableStateFlow(0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val milestones: StateFlow<List<Milestone>> = combine(
        _searchQuery,
        _filter,
        _refreshRequests
    ) { query, filter, refreshCount ->
        Triple(query.trim(), filter, refreshCount)
    }.flatMapLatest { (query, filter, _) ->
        repository.getRootMilestones().map { list ->
            list.filter { milestone ->
                val matchesQuery = query.isBlank() || milestone.title.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    MilestoneFilter.ALL -> true
                    MilestoneFilter.ACTIVE -> !milestone.isCompleted
                    MilestoneFilter.COMPLETED -> milestone.isCompleted
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
        isCompleted: Boolean,
        createdAt: Long
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addMilestone(
                Milestone(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    isCompleted = isCompleted,
                    parentId = null,
                    createdAt = createdAt
                )
            )
        }
    }
}
