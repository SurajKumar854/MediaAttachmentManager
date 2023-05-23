package com.suraj854.trimmodule.interfaces

import android.widget.VideoView
import com.suraj854.trimmodule.models.MediaItem
import java.time.Duration

interface MediaItemClickListener {
    fun onTrimButtonClick()
    fun onNonVideoItemClick()

}