package com.suraj854.trimmodule.utilis

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MediaTypeUtils(){
    enum class MediaType {
        IMAGE, VIDEO, UNKNOWN
    }
    companion object MediaUtils {
        private lateinit var context: Context

        fun initialize(context: Context) {
            this.context = context.applicationContext
        }

        fun getMediaType(uri: Uri): MediaType {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            return when {
                mimeType?.startsWith("image/") == true -> MediaType.IMAGE
                mimeType?.startsWith("video/") == true -> MediaType.VIDEO
                else -> MediaType.UNKNOWN
            }
        }

         fun checkCamStoragePer(activityCompat: Activity): Boolean {
            return checkPermission(activityCompat,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        private fun checkPermission(activityCompat: Activity,vararg permissions: String): Boolean {
            var allPermitted = false
            for (permission in permissions) {
                allPermitted = (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED)
                if (!allPermitted) break
            }
            if (allPermitted) return true
            ActivityCompat.requestPermissions(
                activityCompat, permissions, 220
            )
            return false
        }

        fun convertSecondsToTime(seconds: Long): String? {
            var timeStr: String? = null
            var hour = 0
            var minute = 0
            var second = 0
            if (seconds <= 0) return "00:00" else {
                minute = seconds.toInt() / 60
                if (minute < 60) {
                    second = seconds.toInt() % 60
                    timeStr = unitFormat(minute) + ":" + unitFormat(second)
                } else {
                    hour = minute / 60
                    if (hour > 99) return "99:59:59"
                    minute = minute % 60
                    second = (seconds - hour * 3600 - minute * 60).toInt()
                    timeStr =
                        unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(
                            second
                        )
                }
            }
            return timeStr
        }

        private fun unitFormat(i: Int): String? {
            var retStr: String? = null
            retStr = if (i >= 0 && i < 10) "0" + Integer.toString(i) else "" + i
            return retStr
        }
        fun getVideoDuration(uri: Uri): Long {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toLongOrNull() ?: 0L
            retriever.release()
            return duration
        }
    }



}