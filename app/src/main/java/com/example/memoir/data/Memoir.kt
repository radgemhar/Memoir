package com.example.memoir.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemoirTag {
    PLAIN, IDEA, TASK, MEMORY, URGENT
}

const val DEFAULT_HIGHLIGHT_COLOR: Long = 0L
const val DELETED_RETENTION_MILLIS: Long = 30L * 24L * 60L * 60L * 1000L

@Entity(tableName = "memoirs")
data class Memoir(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val tag: MemoirTag = MemoirTag.PLAIN,
    @ColumnInfo(defaultValue = "4288455599")
    val highlightColor: Long = DEFAULT_HIGHLIGHT_COLOR,
    @ColumnInfo(defaultValue = "'All'")
    val folderName: String = DEFAULT_FOLDER_NAME,
    val moodEmoji: String? = null,
    val moodLabel: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)
