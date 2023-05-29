package com.suraj854.trimmodule.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import com.suraj854.trimmodule.room.entity.MediaAttachmentDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaAttachmentViewModel @Inject constructor(
    val mediaDao: MediaAttachmentDao,
    @ApplicationContext val content: Context
) :
    ViewModel() {
    val mediaItems: kotlinx.coroutines.flow.Flow<List<MediaItemEntity>> =
        mediaDao.getMediaAttachments()


    fun mediaItem(index: Int): kotlinx.coroutines.flow.Flow<MediaItemEntity> = flow {
        //  emit(mediaDao.getMediaAttachment(index))
    }

    init {


    }


    fun insertMediaItem(mediaItemEntity: MediaItemEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaDao.insertMediaItem(mediaItemEntity)
            Log.e("insertMediaItem", "insertMediaItem")
        }
    }

    fun getMediaItem(index: Int): Flow<MediaItemEntity> = flow {
        val index = index
        emit(mediaDao.getMediaAttachment((index + 1).toString()))
    }

    fun updateMediaItem(mediaItemEntity: MediaItemEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaDao.updateMediaPosition(mediaItemEntity)
            Log.e("insertMediaItem", "insertMediaItem")
        }
    }


    fun cleanAttachment() {
        CoroutineScope(Dispatchers.IO).launch {
            mediaDao.clearMediaAttachments()

        }
    }


}