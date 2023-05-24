package com.suraj854.trimmodule.models

data class UploadAttachmentRequest(
    var id: Int,
    val path: String,
    val isVideo: Boolean,
    val trimFromStart: Long,
    val trimFromEnd: Long,
    val lastLeftThumbPosition: Float = 0f,
    val lastRightThumbPosition: Float = 0f
) {
}