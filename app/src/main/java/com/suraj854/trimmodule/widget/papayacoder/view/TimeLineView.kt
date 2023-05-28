package com.papayacoders.customvideocropper.video_trimmer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
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
        if (videoUri != null && w != oldW) getBitmap()
    }

    private var myCoroutineJob: Job? = null
    fun getBitmap() {
        // Set thumbnail properties (Thumbs are squares)


        @Suppress("UnnecessaryVariable") var numThumbs = 0/*  val thumbSize = viewHeight
          val numThumbs = Math.ceil((viewWidth.toFloat() / thumbSize).toDouble()).toInt()*/
        bitmapList.clear()/*      if (isInEditMode) {
                  val bitmap = ThumbnailUtils.extractThumbnail(
                      BitmapFactory.decodeResource(resources, android.R.drawable.sym_def_app_icon)!!, thumbSize, thumbSize
                  )
                  for (i in 0 until numThumbs)
                      bitmapList.add(bitmap)
                  return
              }*/

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

    }
    private fun returnBitmaps(thumbnailList: ArrayList<Bitmap?>) {
        CoroutineScope(Dispatchers.Main).launch {
            bitmapList.clear()
            bitmapList.addAll(thumbnailList)
            invalidate()
        }

        /*   UiThreadExecutor.runTask("", Runnable {
               bitmapList.clear()
               bitmapList.addAll(thumbnailList)
               invalidate()
           }, 0L)*/
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

    lateinit var mediaItem: MediaItem
    fun setVideo(data: Uri, mediaItem: MediaItem) {

        videoUri = data
        this.mediaItem = mediaItem
    }
}
