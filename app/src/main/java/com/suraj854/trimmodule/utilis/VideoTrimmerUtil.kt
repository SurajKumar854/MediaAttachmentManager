package com.suraj854.trimmodule.utilis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import com.suraj854.videotrimmerview.utilis.BaseUtils
import com.suraj854.videotrimmerview.utilis.DeviceUtil
import com.suraj854.videotrimmerview.utilis.UnitConverter


class VideoTrimmerUtil() {


    companion object VideoTrimmerUtil {


        private val TAG = VideoTrimmerUtil::class.java.simpleName
        const val MIN_SHOOT_DURATION = 3000L
        const val VIDEO_MAX_TIME = 10
        const val MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L
        const val MAX_COUNT_RANGE = 10 //seekBar
        private var SCREEN_WIDTH_FULL = 0

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        var RECYCLER_VIEW_PADDING = 0

        var THUMB_WIDTH = 0

        var VIDEO_FRAMES_WIDTH = 0
        var THUMB_HEIGHT = 0
        fun getVideoFilePath(url: String): String {
            var url = url
            if (TextUtils.isEmpty(url) || url.length < 5) return ""
            if (url.substring(0, 4).equals("http", ignoreCase = true)) {
            } else {
                url = "file://$url"
            }
            return url
        }

        fun initialize(context: Context) {
            this.context = context.applicationContext
            DeviceUtil.initialize(this.context)
            BaseUtils.init(this.context)
            SCREEN_WIDTH_FULL = DeviceUtil.deviceWidth
            VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2
            RECYCLER_VIEW_PADDING = UnitConverter().dpToPx(35)
            THUMB_HEIGHT = UnitConverter().dpToPx(50)
            THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VIDEO_MAX_TIME
        }

         fun convertSecondsToTime(seconds: Long): String {
            var timeStr: String? = null
            var hour = 0
            var minute = 0
            var second = 0
            if (seconds <= 0) {
                return "00:00"
            } else {
                minute = seconds.toInt() / 60
                if (minute < 60) {
                    second = seconds.toInt() % 60
                    timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second)
                } else {
                    hour = minute / 60
                    if (hour > 99) return "99:59:59"
                    minute = minute % 60
                    second = (seconds - hour * 3600 - minute * 60).toInt()
                    timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
                }
            }
            return timeStr
        }

        private fun unitFormat(i: Int): String {
            var retStr: String? = null
            retStr = if (i >= 0 && i < 10) {
                "0" + Integer.toString(i)
            } else {
                "" + i
            }
            return retStr
        }



    }






}