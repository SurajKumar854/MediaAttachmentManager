package com.papayacoders.customvideocropper.video_trimmer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.papayacoders.customvideocropper.video_trimmer.utils.UiThreadExecutor
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
import com.suraj854.trimmodule.widget.papayacoder.BackgroundExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


open class TimeLineView @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private var videoUri: Uri? = null

    @Suppress("LeakingThis")
//    private var bitmapList: LongSparseArray<Bitmap>? = null
    private val bitmapList = ArrayList<Bitmap?>()


    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (videoUri != null && w != oldW)
            getBitmap(w, h)
    }

    private var myCoroutineJob: Job? = null
  /*  fun getBitmap() {
        // Set thumbnail properties (Thumbs are squares)


        @Suppress("UnnecessaryVariable") var numThumbs = 0*//*  val thumbSize = viewHeight
          val numThumbs = Math.ceil((viewWidth.toFloat() / thumbSize).toDouble()).toInt()*//*
        bitmapList.clear()*//*      if (isInEditMode) {
                  val bitmap = ThumbnailUtils.extractThumbnail(
                      BitmapFactory.decodeResource(resources, android.R.drawable.sym_def_app_icon)!!, thumbSize, thumbSize
                  )
                  for (i in 0 until numThumbs)
                      bitmapList.add(bitmap)
                  return
              }*//*

        val startPosition = 0
        myCoroutineJob = CoroutineScope(Dispatchers.Default).launch {

            val thumbnailList = ArrayList<Bitmap?>()
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, videoUri)
            // Retrieve media data

            numThumbs =
                (((mediaItem.duration * 1.0f / (VideoTrimmerUtil.MAX_SHOOT_DURATION.toFloat()) * 10)).toInt())
            Log.e("numThumbs", "$numThumbs")
            val interval = (mediaItem.duration - startPosition) / (numThumbs - 1)
            for (i in 0 until numThumbs) {
                var bitmap: Bitmap? = mediaMetadataRetriever.getFrameAtTime(
                    i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                Log.e("bitmap", "$i")
                if (bitmap != null)


                    bitmap = bitmap?.let {
                        Bitmap.createScaledBitmap(
                            it,
                            VideoTrimmerUtil.THUMB_WIDTH,
                            VideoTrimmerUtil.THUMB_HEIGHT,
                            false
                        )

                    }
                thumbnailList.add(bitmap)
            }
            mediaMetadataRetriever.release()
            returnBitmaps(thumbnailList)


        }

    }*/
    private fun returnBitmaps(thumbnailList: ArrayList<Bitmap?>) {
      /*  CoroutineScope(Dispatchers.Main).launch {
            bitmapList.clear()
            bitmapList.addAll(thumbnailList)
            invalidate()
        }*/

         UiThreadExecutor.runTask("", Runnable {
               bitmapList.clear()
               bitmapList.addAll(thumbnailList)
               invalidate()
           }, 0L)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        var x = 0
        val thumbSize = VideoTrimmerUtil.THUMB_HEIGHT
        for (bitmap in bitmapList) {

            if (bitmap != null) Log.e("Surrasfsa", "Drawing")
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
            }
            x += thumbSize
        }
    }

    fun getBitmap(viewWidth: Int, viewHeight: Int) {
        // Set thumbnail properties (Thumbs are squares)
        @Suppress("UnnecessaryVariable")
        val thumbSize = viewHeight
        val numThumbs = Math.ceil((viewWidth.toFloat() / thumbSize).toDouble()).toInt()
        bitmapList.clear()
        if (isInEditMode) {
            val bitmap = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeResource(resources, android.R.drawable.sym_def_app_icon)!!, thumbSize, thumbSize
            )
            for (i in 0 until numThumbs)
                bitmapList.add(bitmap)
            return
        }
        try {
            BackgroundExecutor.cancelAll("", true)
            BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
                override fun execute() {
                    try {
                        val thumbnailList = ArrayList<Bitmap?>()
                        val mediaMetadataRetriever = MediaMetadataRetriever()
                        mediaMetadataRetriever.setDataSource(context, videoUri)
                        // Retrieve media data
                        val videoLengthInMs = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong() * 1000L
                        val interval = videoLengthInMs / numThumbs
                        for (i in 0 until numThumbs) {
                            var bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
                                mediaMetadataRetriever.getScaledFrameAtTime(
                                    i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, thumbSize, thumbSize
                                )
                            else mediaMetadataRetriever.getFrameAtTime(
                                i * interval,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                            if (bitmap != null)
                                bitmap = ThumbnailUtils.extractThumbnail(bitmap, thumbSize, thumbSize)
                            thumbnailList.add(bitmap)
                        }
                        mediaMetadataRetriever.release()
                        returnBitmaps(thumbnailList)
                    } catch (e: Throwable) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                    }

                }
            }
            )
        } catch (e: Exception) {
        }
    }

    lateinit var mediaItem: MediaItem
    fun setVideo(data: Uri, mediaItem: MediaItem) {

        videoUri = data
        this.mediaItem = mediaItem
    }
}
