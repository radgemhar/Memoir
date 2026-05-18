package com.example.memoir.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.Memoir
import com.example.memoir.data.MemoirRepository
import com.example.memoir.data.MemoirTag
import com.example.memoir.data.Milestone
import com.example.memoir.data.MilestoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class DeskViewModel @Inject constructor(
    private val memoirRepository: MemoirRepository,
    private val milestoneRepository: MilestoneRepository
) : ViewModel() {

    var id by mutableStateOf<String?>(null)
        private set

    var isMilestoneMode by mutableStateOf(false)
        private set

    var title by mutableStateOf("")
    var body by mutableStateOf("")
    var tag by mutableStateOf(MemoirTag.PLAIN)
    var moodEmoji by mutableStateOf<String?>(null)
    var moodLabel by mutableStateOf<String?>(null)
    var isPinned by mutableStateOf(false)
    var isArchived by mutableStateOf(false)
    var createdAt by mutableStateOf(System.currentTimeMillis())

    private var initialContent = ""
    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()

    fun load(id: String?, isMilestone: Boolean) {
        this.id = id
        this.isMilestoneMode = isMilestone
        if (id != null) {
            viewModelScope.launch {
                if (isMilestone) {
                    milestoneRepository.getMilestoneById(id)?.let { milestone ->
                        title = milestone.title
                        body = ""
                        createdAt = milestone.createdAt
                    }
                } else {
                    memoirRepository.getMemoirById(id)?.let { memoir ->
                        title = memoir.title
                        body = memoir.body
                        tag = memoir.tag
                        moodEmoji = memoir.moodEmoji
                        moodLabel = memoir.moodLabel
                        isPinned = memoir.isPinned
                        isArchived = memoir.isArchived
                        createdAt = memoir.createdAt
                    }
                }
                initialContent = "$title|$body"
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        saveToUndo(title)
        title = newTitle
    }

    fun onBodyChange(newBody: String) {
        saveToUndo(body)
        body = newBody
    }

    private fun saveToUndo(oldValue: String) {
        if (undoStack.isEmpty() || undoStack.peek() != oldValue) {
            undoStack.push(oldValue)
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(if (isMilestoneMode) title else body)
            val prev = undoStack.pop()
            if (isMilestoneMode) title = prev else body = prev
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(if (isMilestoneMode) title else body)
            val next = redoStack.pop()
            if (isMilestoneMode) title = next else body = next
        }
    }

    fun save() {
        if (title.isBlank() && body.isBlank()) return
        
        viewModelScope.launch {
            if (isMilestoneMode) {
                val milestone = Milestone(
                    id = id ?: java.util.UUID.randomUUID().toString(),
                    title = title,
                    isCompleted = false,
                    parentId = null,
                    createdAt = createdAt
                )
                milestoneRepository.addMilestone(milestone)
            } else {
                val memoir = Memoir(
                    id = id ?: java.util.UUID.randomUUID().toString(),
                    title = title,
                    body = body,
                    tag = tag,
                    moodEmoji = moodEmoji,
                    moodLabel = moodLabel,
                    isPinned = isPinned,
                    isArchived = isArchived,
                    createdAt = createdAt,
                    lastModified = System.currentTimeMillis()
                )
                if (id == null) memoirRepository.addMemoir(memoir)
                else memoirRepository.updateMemoir(memoir)
            }
        }
    }
}
