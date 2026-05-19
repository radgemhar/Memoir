package com.example.memoir.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.MemoirFolder
import com.example.memoir.data.MemoirFolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: MemoirFolderRepository
) : ViewModel() {
    val folders: StateFlow<List<MemoirFolder>> = folderRepository.getFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addFolder(name: String) {
        viewModelScope.launch {
            folderRepository.addFolder(name)
        }
    }

    fun renameFolder(oldName: String, newName: String) {
        viewModelScope.launch {
            folderRepository.renameFolder(oldName, newName)
        }
    }

    fun deleteFolder(name: String) {
        viewModelScope.launch {
            folderRepository.deleteFolder(name)
        }
    }

    fun selectFolder(name: String) {
        folderRepository.setSelectedFolder(name)
    }
}
