package com.suraj854.trimmodule.models

data class MediaItem(
    val path: String,
    val duration: Long = 0,
    val isVideo: Boolean,
    var trimFromStart: Long=0,
    var trimFromEnd: Long=0,
    var lastLeftThumbPosition: Float = 0f,
    var lastRightThumbPosition: Float = 1f
)