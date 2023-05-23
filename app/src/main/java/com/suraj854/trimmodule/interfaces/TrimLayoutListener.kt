package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem

interface TrimLayoutListener {
    fun showTrimLayout()
    fun hideTrimLayout()
   suspend fun trimMediaItemListener(mediaItem: MediaItem)

   suspend fun trimVideoVideoListener(video: VideoView)
}