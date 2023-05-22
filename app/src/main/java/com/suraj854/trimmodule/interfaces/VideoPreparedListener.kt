package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem

interface VideoPreparedListener {
    fun onVideoPrepared(videoView: VideoView)
    fun onMediaItemListener(mediaItem: MediaItem)
}