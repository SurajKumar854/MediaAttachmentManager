package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem
import java.text.FieldPosition

interface TrimLayoutListener {
    fun showTrimLayout()
    fun hideTrimLayout()
    fun onMediaChange(position: Int,mediaItem: MediaItem)
   suspend fun trimMediaItemListener(mediaItem: MediaItem)

   suspend fun trimVideoVideoListener(video: VideoView)

}