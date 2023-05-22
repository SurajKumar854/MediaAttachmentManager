package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem
import java.time.Duration

interface MediaItemClickListener {
    fun onTrimButtonClick(mediaItem: MediaItem, videoViewAtPosition: VideoView,duration: Long)
    fun onNonVideoItemClick()

}