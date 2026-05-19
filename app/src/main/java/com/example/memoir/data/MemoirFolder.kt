package com.example.memoir.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_FOLDER_NAME = "All"

@Entity(tableName = "memoir_folders")
data class MemoirFolder(
    @PrimaryKey val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
