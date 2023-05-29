package com.suraj854.trimmodule.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.suraj854.trimmodule.MediaApplication
import com.suraj854.trimmodule.room.AppDatabase
import com.suraj854.trimmodule.room.entity.MediaAttachmentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideApplication(): Application = MediaApplication()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "PlannerDB"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    @Singleton
    fun provideItemDao(appDatabase: AppDatabase): MediaAttachmentDao {
        return appDatabase.mediaAttachmentDao()
    }

    //


}