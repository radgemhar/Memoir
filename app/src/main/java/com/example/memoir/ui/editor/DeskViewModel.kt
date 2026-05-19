package com.example.memoir.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoir.data.DEFAULT_HIGHLIGHT_COLOR
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
    var highlightColor by mutableStateOf(DEFAULT_HIGHLIGHT_COLOR)
    var moodEmoji by mutableStateOf<String?>(null)
    var moodLabel by mutableStateOf<String?>(null)
    var isPinned by mutableStateOf(false)
    var isArchived by mutableStateOf(false)
    var createdAt by mutableStateOf(System.currentTimeMillis())

    var isDirty by mutableStateOf(false)

    private var initialContent = ""
    private val undoStack = mutableListOf<Pair<String, String>>()
    private val redoStack = mutableListOf<Pair<String, String>>()
    private var isUpdatingFromUndoRedo = false
    private var lastSavedState: Pair<String, String>? = null

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
                        highlightColor = memoir.highlightColor
                        moodEmoji = memoir.moodEmoji
                        moodLabel = memoir.moodLabel
                        isPinned = memoir.isPinned
                        isArchived = memoir.isArchived
                        createdAt = memoir.createdAt
                    }
                }
                initialContent = "$title|$body"
                lastSavedState = title to body
            }
        } else {
            lastSavedState = title to body
        }
    }

    fun onHighlightColorChange(newColor: Long) {
        if (highlightColor != newColor) {
            highlightColor = newColor
            isDirty = true
        }
    }

    fun onTitleChange(newTitle: String) {
        if (title != newTitle) {
            if (!isUpdatingFromUndoRedo) {
                addToUndoStack()
            }
            title = newTitle
            isDirty = true
        }
    }

    fun onBodyChange(newBody: String) {
        if (body != newBody) {
            if (!isUpdatingFromUndoRedo) {
                addToUndoStack()
            }
            body = newBody
            isDirty = true
        }
    }

    private fun addToUndoStack() {
        val currentState = title to body
        // Only add to undo stack if the state is different from what we last saved as a history point
        if (undoStack.isEmpty() || undoStack.last() != currentState) {
            undoStack.add(currentState)
            // Limit stack size to prevent memory issues
            if (undoStack.size > 50) undoStack.removeAt(0)
        }
        // ALWAYS clear redo stack when a NEW user action happens
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            isUpdatingFromUndoRedo = true
            redoStack.add(title to body)
            val last = undoStack.removeAt(undoStack.size - 1)
            title = last.first
            body = last.second
            isDirty = true
            isUpdatingFromUndoRedo = false
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isUpdatingFromUndoRedo = true
            undoStack.add(title to body)
            val last = redoStack.removeAt(redoStack.size - 1)
            title = last.first
            body = last.second
            isDirty = true
            isUpdatingFromUndoRedo = false
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
                    highlightColor = highlightColor,
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
            isDirty = false
        }
    }

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()

    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }
}
