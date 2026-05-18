package com.example.memoir.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.Milestone
import com.example.memoir.data.MilestoneRepository
import com.example.memoir.ui.home.ChronicleFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MilestonesViewModel @Inject constructor(
    private val repository: MilestoneRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ChronicleFilter.ACTIVE)
    val filter: StateFlow<ChronicleFilter> = _filter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val milestones: StateFlow<List<Milestone>> = combine(_filter, _searchQuery) { currentFilter, query ->
        currentFilter to query
    }.flatMapLatest { (currentFilter, query) ->
        repository.getRootMilestones()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: ChronicleFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
}
