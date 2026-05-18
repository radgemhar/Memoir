package com.example.memoir.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemoirTag {
    PLAIN, IDEA, TASK, MEMORY, URGENT
}

@Entity(tableName = "memoirs")
data class Memoir(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val tag: MemoirTag = MemoirTag.PLAIN,
    val moodEmoji: String? = null,
    val moodLabel: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)
