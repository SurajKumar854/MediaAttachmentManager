package com.suraj854.trimmodule.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import com.suraj854.trimmodule.room.entity.MediaAttachmentDao


@Database(entities = [MediaItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mediaAttachmentDao(): MediaAttachmentDao

    companion object {
        private const val DATABASE_NAME = "attachment_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = context.applicationContext?.let {
                        Room.databaseBuilder(it, AppDatabase::class.java, DATABASE_NAME)
                            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
                    }
                }
            }
            return INSTANCE
        }

    }


}
