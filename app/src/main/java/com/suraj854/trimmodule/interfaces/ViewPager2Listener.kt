package com.suraj854.trimmodule.interfaces

import com.suraj854.trimmodule.models.MediaItem

interface ViewPager2Listener {
    fun onScrollPosition(int:Int,mediaItem: MediaItem)
}