package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem

interface MediaItemClickListener {
    fun onTrimButtonClick(mediaItem: MediaItem, videoViewAtPosition: VideoView)
    fun onNonVideoItemClick()

}