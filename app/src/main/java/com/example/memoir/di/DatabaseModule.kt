package com.example.memoir.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memoir.data.MemoirDao
import com.example.memoir.data.MemoirDatabase
import com.example.memoir.data.MemoirFolderDao
import com.example.memoir.data.MilestoneDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE memoirs ADD COLUMN highlightColor INTEGER NOT NULL DEFAULT 4288455599"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE memoirs ADD COLUMN folderName TEXT NOT NULL DEFAULT 'All'")
            db.execSQL("ALTER TABLE memoirs ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS memoir_folders (" +
                    "name TEXT NOT NULL PRIMARY KEY, " +
                    "createdAt INTEGER NOT NULL)"
            )
            db.execSQL("INSERT OR IGNORE INTO memoir_folders(name, createdAt) VALUES('All', 0)")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE milestones ADD COLUMN folderName TEXT NOT NULL DEFAULT 'All'")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MemoirDatabase {
        return Room.databaseBuilder(
            context,
            MemoirDatabase::class.java,
            "memoir_db"
        )
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMemoirDao(database: MemoirDatabase): MemoirDao {
        return database.memoirDao()
    }

    @Provides
    fun provideMemoirFolderDao(database: MemoirDatabase): MemoirFolderDao {
        return database.memoirFolderDao()
    }

    @Provides
    fun provideMilestoneDao(database: MemoirDatabase): MilestoneDao {
        return database.milestoneDao()
    }
}
