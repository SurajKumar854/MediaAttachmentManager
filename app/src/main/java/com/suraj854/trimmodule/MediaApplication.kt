package com.suraj854.trimmodule

import android.app.Application
import com.suraj854.trimmodule.repository.MediaRepository
import com.suraj854.trimmodule.room.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaApplication :Application() {
    val database by lazy { AppDatabase.getAppDataBase(this) }
    val repository by lazy { database?.mediaAttachmentDao()?.let { MediaRepository(this, it) } }
    override fun onCreate() {
        super.onCreate()
    }
}