package com.suraj854.trimmodule.interfaces

import com.suraj854.trimmodule.models.MediaItem
import java.text.FieldPosition

interface ViewPager2Listener {
    fun onScrollPosition(int:Int,mediaItem: MediaItem)
    fun mediaPlackHandler(position: Int,mediaItem: MediaItem)
}