package com.example.memoir.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.Memoir
import com.example.memoir.data.MemoirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyDeletedViewModel @Inject constructor(
    private val repository: MemoirRepository
) : ViewModel() {
    val memoirs: StateFlow<List<Memoir>> = repository.getRecentlyDeletedMemoirs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.purgeExpiredRecentlyDeleted()
        }
    }

    fun restore(memoir: Memoir) {
        viewModelScope.launch {
            repository.restoreMemoir(memoir)
        }
    }

    fun deletePermanently(memoir: Memoir) {
        viewModelScope.launch {
            repository.deleteMemoirPermanently(memoir)
        }
    }

    fun emptyAll() {
        viewModelScope.launch {
            repository.deleteAllRecentlyDeleted()
        }
    }
}
