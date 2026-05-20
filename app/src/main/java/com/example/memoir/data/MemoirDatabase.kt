package com.example.memoir.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromTag(tag: MemoirTag): String = tag.name

    @TypeConverter
    fun toTag(name: String): MemoirTag = MemoirTag.valueOf(name)
}

@Database(
    entities = [Memoir::class, MemoirFolder::class, Milestone::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MemoirDatabase : RoomDatabase() {
    abstract fun memoirDao(): MemoirDao
    abstract fun memoirFolderDao(): MemoirFolderDao
    abstract fun milestoneDao(): MilestoneDao
}
