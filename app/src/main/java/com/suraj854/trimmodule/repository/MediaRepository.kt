package com.suraj854.trimmodule.repository

import android.content.Context
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import com.suraj854.trimmodule.room.entity.MediaAttachmentDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaItemDao: MediaAttachmentDao
) {
    val mediaItems: Flow<List<MediaItemEntity>> = mediaItemDao.getMediaAttachments()



}