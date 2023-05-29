package com.suraj854.trimmodule.room.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suraj854.videotrimmerview.utilis.UnitConverter

@Entity(tableName = "MediaItems")
data class MediaItemEntity(
    val path: String,
    val duration: Long,
    val isVideo: Boolean,
    var leftProgress: Long,
    var rightProgress: Long,
    var trimFromStart: Long,
    var trimFromEnd: Long,
    var lastLeftThumbPosition: Float = 0f,
    var lastRightThumbPosition: Float = UnitConverter().dpToPx(385f),
) {
    @PrimaryKey()
    var id: Int? = null
}