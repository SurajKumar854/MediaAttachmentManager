package com.suraj854.trimmodule.models

data class MediaItem(
    val path: String,
    val duration: Long = 0,
    val isVideo: Boolean,
    var frameIndex: Int = 0,
    var trimFromStart: Long = 0,
    var trimFromEnd: Long = 0,
    var lastLeftThumbPosition: Double = 0.0,
    var lastRightThumbPosition: Double = 1.0,
    var leftProgress: Long,
    var rightProgress: Long
)