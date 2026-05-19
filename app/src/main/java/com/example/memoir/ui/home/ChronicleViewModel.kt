package com.example.memoir.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.DEFAULT_FOLDER_NAME
import com.example.memoir.data.Memoir
import com.example.memoir.data.MemoirFolderRepository
import com.example.memoir.data.MemoirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChronicleViewModel @Inject constructor(
    private val repository: MemoirRepository,
    private val folderRepository: MemoirFolderRepository
) : ViewModel() {

    private val _selectedFolder = MutableStateFlow(DEFAULT_FOLDER_NAME)
    val selectedFolder: StateFlow<String> = _selectedFolder

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _refreshRequests = MutableStateFlow(0)

    val folders: StateFlow<List<String>> = folderRepository.getFolders()
        .map { folders -> folders.map { it.name }.ifEmpty { listOf(DEFAULT_FOLDER_NAME) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(DEFAULT_FOLDER_NAME)
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val memoirs: StateFlow<List<Memoir>> = combine(
        _selectedFolder,
        _searchQuery,
        _refreshRequests
    ) { selectedFolder, query, _ ->
        selectedFolder to query
    }.flatMapLatest { (selectedFolder, query) ->
        if (query.isNotBlank()) {
            repository.searchMemoirs(query, selectedFolder)
        } else {
            repository.getActiveMemoirs(selectedFolder)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            folderRepository.ensureDefaultFolder()
            repository.purgeExpiredRecentlyDeleted()
        }
    }

    fun setSelectedFolder(folderName: String) {
        _selectedFolder.value = folderName
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            folderRepository.addFolder(name)
            if (name.trim().isNotBlank()) {
                _selectedFolder.value = name.trim()
            }
        }
    }

    fun moveMemoirToFolder(memoir: Memoir, folderName: String) {
        viewModelScope.launch {
            repository.moveToFolder(memoir, folderName)
        }
    }

    fun refresh() {
        _refreshRequests.value += 1
        viewModelScope.launch {
            repository.purgeExpiredRecentlyDeleted()
        }
    }

    fun discardMemoir(memoir: Memoir, onUndo: (Memoir) -> Unit) {
        viewModelScope.launch {
            repository.moveToRecentlyDeleted(memoir)
            onUndo(memoir)
        }
    }

    fun restoreMemoir(memoir: Memoir) {
        viewModelScope.launch {
            repository.restoreMemoir(memoir)
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
