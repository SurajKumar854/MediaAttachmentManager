package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem

interface VideoPreparedListener {
    suspend fun onVideoPrepared(videoView: VideoView)
    suspend fun onMediaItemListener(mediaItem: MediaItem)
}