package com.suraj854.trimmodule.utilis

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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

    }
}