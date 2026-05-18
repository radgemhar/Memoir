package com.example.memoir.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.Memoir
import com.example.memoir.data.MemoirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChronicleFilter {
    ALL, ACTIVE, ARCHIVED
}

@HiltViewModel
class ChronicleViewModel @Inject constructor(
    private val repository: MemoirRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ChronicleFilter.ACTIVE)
    val filter: StateFlow<ChronicleFilter> = _filter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val memoirs: StateFlow<List<Memoir>> = combine(_filter, _searchQuery) { currentFilter, query ->
        currentFilter to query
    }.flatMapLatest { (currentFilter, query) ->
        if (query.isNotBlank()) {
            repository.searchMemoirs(query)
        } else {
            when (currentFilter) {
                ChronicleFilter.ACTIVE -> repository.getActiveMemoirs()
                ChronicleFilter.ARCHIVED -> repository.getArchivedMemoirs()
                ChronicleFilter.ALL -> {
                    combine(repository.getActiveMemoirs(), repository.getArchivedMemoirs()) { active, archived ->
                        (active + archived).sortedWith(
                            compareByDescending<Memoir> { it.isPinned }.thenByDescending { it.createdAt }
                        )
                    }
                }
            }
        }
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

    fun discardMemoir(memoir: Memoir, onUndo: (Memoir) -> Unit) {
        viewModelScope.launch {
            repository.deleteMemoir(memoir)
            onUndo(memoir)
        }
    }

    fun restoreMemoir(memoir: Memoir) {
        viewModelScope.launch {
            repository.addMemoir(memoir)
        }
    }

    fun preserveMemoir(memoir: Memoir) {
        viewModelScope.launch {
            repository.archiveMemoir(memoir)
        }
    }

    fun unarchiveMemoir(memoir: Memoir) {
        viewModelScope.launch {
            repository.unarchiveMemoir(memoir)
        }
    }

    fun togglePin(memoir: Memoir, onMaxReached: () -> Unit) {
        viewModelScope.launch {
            if (!memoir.isPinned) {
                val pinnedCount = memoirs.value.count { it.isPinned && !it.isArchived }
                if (pinnedCount >= 5) {
                    onMaxReached()
                    return@launch
                }
            }
            repository.togglePin(memoir)
        }
    }
}
