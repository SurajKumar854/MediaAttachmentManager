package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem

interface TrimLayoutListener {
    fun showTrimLayout(mediaItem: MediaItem, videoViewAtPosition: VideoView)
    fun hideTrimLayout()

    fun trimVideoVideoListener(video: VideoView)
}