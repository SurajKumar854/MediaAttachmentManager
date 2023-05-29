package com.suraj854.trimmodule.room.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface MediaAttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediaItem(item: MediaItemEntity)

    @Query("Select * from MediaItems")
    fun getMediaAttachments(): Flow<List<MediaItemEntity>>

    @Query("Select * from MediaItems where  id =:id")
    fun getMediaAttachment(id: String): MediaItemEntity

    @Update()
    fun updateMediaPosition(mediaItemEntity: MediaItemEntity)

    @Query("DELETE FROM MediaItems")
    fun clearMediaAttachments()


}