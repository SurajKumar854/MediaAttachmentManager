package com.suraj854.videotrimmerview.utilis

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.pm.PackageManager

class DeviceUtil() {
    companion object DeviceUtil {

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context
        fun initialize(context: Context) {
            this.context = context.applicationContext
        }



        val deviceWidth: Int
            get() = context.resources.displayMetrics.widthPixels


        val deviceHeight: Int
            get() = context.resources.displayMetrics.heightPixels


}}