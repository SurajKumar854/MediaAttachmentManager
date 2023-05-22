package com.suraj854.trimmodule.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.source.TrimDataSource
import com.otaliastudios.transcoder.source.UriDataSource
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.adapters.VideoTrimmerAdapter
import com.suraj854.trimmodule.fragments.MediaAttachmentFragment
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.utilis.MediaTypeUtils
import com.suraj854.trimmodule.utilis.MediaTypeUtils.MediaUtils.checkCamStoragePer
import com.suraj854.trimmodule.utilis.MediaTypeUtils.MediaUtils.getMediaType
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil.VideoTrimmerUtil.MAX_COUNT_RANGE
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil.VideoTrimmerUtil.MAX_SHOOT_DURATION
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil.VideoTrimmerUtil.RECYCLER_VIEW_PADDING
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil.VideoTrimmerUtil.THUMB_WIDTH
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH
import com.suraj854.videotrimmerview.widget.RangeSeekBarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaAttachmentActivity : AppCompatActivity(), TrimLayoutListener {
    lateinit var fragmentContainer: FrameLayout
    lateinit var trimLL: LinearLayout
    lateinit var addMediaBtn: Button
    private val fragment: MediaAttachmentFragment = MediaAttachmentFragment()
    private var mPlayView: ImageView? = null
    lateinit var video_frames_recyclerView: RecyclerView
    lateinit var frameAdapter: VideoTrimmerAdapter
    lateinit var seekBarLayout: LinearLayout
    lateinit var mRangeSeekBarView: RangeSeekBarView
    lateinit var mRedProgressIcon: ImageView
    lateinit var mVideoView: VideoView
    lateinit var mPostBtn: Button
    lateinit var mTrimBtn: TextView

    private val endPosition = 0
    private val startPosition = 0
    private var isOverScaledTouchSlop = false
    private val mScaledTouchSlop = 0
    private var mMaxWidth: Int = 0
    private var lastScrollX = 0
    private var mLeftProgressPos: Long = 0
    private var mRightProgressPos: Long = 0
    private var mRedProgressBarPos: Long = 0
    private var mAverageMsPx = 0f
    private var averagePxMs = 0f
    private var mDuration: Int = 0
    private var mThumbsTotalCount = 0
    private var scrollPos: Long = 0
    private var isSeeking = false

    init {

    }

    private val addMediaChooserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK && result.getData() != null) {

                if (result.data?.clipData != null) {
                    // Multiple items selected
                    val mediaItemCount = result.data?.let {
                        it.clipData?.itemCount
                    }

                    for (i in 0 until mediaItemCount!!) {
                        val uri = result.data?.let {
                            it.clipData?.getItemAt(i)
                        }

                        val mediaType = uri?.uri?.let { getMediaType(it) }
                        if (mediaType == MediaTypeUtils.MediaType.IMAGE) {
                            // Process as an image

                            fragment.addMediaItem(MediaItem(uri.uri.toString(), false))
                        } else if (mediaType == MediaTypeUtils.MediaType.VIDEO) {
                            // Process as a video
                            fragment.addMediaItem(MediaItem(uri.uri.toString(), true))
                        }
                    }

                } else if (result.data != null) {

                }

            }

        }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_attachment)
        MediaTypeUtils.initialize(applicationContext)
        VideoTrimmerUtil.initialize(this)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        addMediaBtn = findViewById(R.id.addMediaBtn)
        fragment.setTrimLayoutListener(this)
        trimLL = findViewById(R.id.trimLL)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        mPlayView = findViewById(R.id.icon_video_play)
        mPostBtn = findViewById(R.id.mPostBtn)
        mTrimBtn = findViewById(R.id.trimBtn)
        seekBarLayout = findViewById(R.id.seekBarLayout)
        mRedProgressIcon = findViewById<ImageView>(R.id.positionIcon)
        mMaxWidth = VIDEO_FRAMES_WIDTH
        video_frames_recyclerView = findViewById(R.id.video_frames_recyclerView)
        video_frames_recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        video_frames_recyclerView.addOnScrollListener(mOnScrollListener)
        frameAdapter = VideoTrimmerAdapter(this)


        video_frames_recyclerView.adapter = frameAdapter

        mPlayView?.setOnClickListener({ playVideoOrPause() })


        addMediaBtn.setOnClickListener {
            if (checkCamStoragePer(this)) {
                openMultipleMedia()
            }

        }
        mPostBtn.setOnClickListener {

        }

        mTrimBtn.setOnClickListener {
            val source = UriDataSource(this, Uri.parse(mediaItem.path));
            val start = mLeftProgressPos
            val end = mRightProgressPos
            val trimFromLeft = mDuration - end

            Log.e("startTrim", " ${mLeftProgressPos / 1000} ")
            Log.e("end", " ${mRightProgressPos / 1000} ")
            Log.e("mDuration", " $mDuration ")
            Log.e("trimfromleft", " $trimFromLeft ")
            val trim =
                TrimDataSource(
                    source, (start * 1000).toLong(), (trimFromLeft * 1000).toLong()
                );
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputName = "trimmedVideo_$timeStamp.mp4"


            mVideoView.pause()
            val folder =
                File("/storage/emulated/0/Android" + "/Trimmed/")

            if (!folder.exists()) {
                folder.mkdirs()
            }
            Log.e("OutPath", "${folder.path}/$outputName")
            CoroutineScope(Dispatchers.Main).launch {
                Transcoder.into("${folder.path}/$outputName")

                    .addDataSource(trim).setListener(object : TranscoderListener {

                        override fun onTranscodeProgress(data: Double) {
                            val progressPercentage = (data * 100).toInt()

                            CoroutineScope(Dispatchers.Main).launch {
                                mTrimBtn.text = "$progressPercentage %"
                            }
                        }

                        override fun onTranscodeCompleted(successCode: Int) {
                            mTrimBtn.text = "Done"

                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                mTrimBtn.text = "Trim"
                            }
                        }

                        override fun onTranscodeCanceled() {

                        }

                        override fun onTranscodeFailed(exception: Throwable) {
                            Log.e("Error on Encode", exception.message.toString())

                        }

                    }).transcode()


            }
        }


    }

    private fun postMediaAttachments() {

    }

    private fun openMultipleMedia() {
        try {
            val intent = Intent()
            intent.type = "*/*"
            val mimeTypes = arrayOf("image/*", "video/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            addMediaChooserResult.launch(Intent.createChooser(intent, "Select Video"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun isPermissionOk(vararg results: Int): Boolean {
        var isAllGranted = true
        for (result in results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false
                break
            }
        }
        return isAllGranted
    }

    lateinit var mediaItem: MediaItem
    override fun showTrimLayout(mediaItem: MediaItem, videoView: VideoView) {
        this.mediaItem = mediaItem
        this.mVideoView = videoView
        this.mVideoView.requestFocus()


        videoView.setOnClickListener {

            if (mVideoView.isPlaying) {
                mVideoView.pause()
                Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show()
                mVideoView.start()
            }
        }

        this.mVideoView.setOnPreparedListener { mp ->
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)

            videoPrepared(Uri.parse(mediaItem.path), mp)
        }



        trimLL.visibility = View.VISIBLE

    }

    private fun videoPrepared(mSourceUri: Uri?, mediaPlayer: MediaPlayer) {
        mDuration = mVideoView.duration



        /*if (!getRestoreState()) {
              seekTo(mRedProgressBarPos.toInt().toLong())
          } else {
              setRestoreState(false)
              seekTo(mRedProgressBarPos.toInt().toLong())
          }*/
        initRangeSeekBarView()
        CoroutineScope(Dispatchers.Main).launch {
            frameAdapter.clearBitmapsList()
            val mediaMetadataRetriever = MediaMetadataRetriever()

            mediaMetadataRetriever.setDataSource(
                this@MediaAttachmentActivity,
                mSourceUri
            )// Retrieve media data use microsecond


            val interval = (mDuration - 0) / (mThumbsTotalCount - 1)
            Log.e("mThumbsTotalCount", mThumbsTotalCount.toString())
            for (i in 0 until mThumbsTotalCount) {
                val frameTime = startPosition + interval * i
                var bitmap: Bitmap? = mediaMetadataRetriever.getFrameAtTime(
                    (frameTime * 1000).toLong(),
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                    ?: continue

                bitmap = bitmap?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        VideoTrimmerUtil.THUMB_WIDTH,
                        VideoTrimmerUtil.THUMB_HEIGHT,
                        false
                    )
                }
                if (bitmap != null) {

                    frameAdapter.addBitmaps(bitmap)
                }

            }
            mediaMetadataRetriever.release()

        }




    }


    override fun hideTrimLayout() {

        trimLL.visibility = View.GONE

    }

    private fun initRangeSeekBarView() {

        mLeftProgressPos = 0
        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = MAX_COUNT_RANGE
            mRightProgressPos = mDuration.toLong()
        } else {

            mThumbsTotalCount =
                (((mDuration * 1.0f / (MAX_SHOOT_DURATION.toFloat()) * 10)).toInt())

            mRightProgressPos = MAX_SHOOT_DURATION

        }
        video_frames_recyclerView.addItemDecoration(
            SpacesItemDecoration2(
                RECYCLER_VIEW_PADDING,
                mThumbsTotalCount
            )
        )
        mRangeSeekBarView =
            RangeSeekBarView(applicationContext, mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView.selectedMinValue = mLeftProgressPos
        mRangeSeekBarView.selectedMaxValue = mRightProgressPos
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView.setMinShootTime(VideoTrimmerUtil.MIN_SHOOT_DURATION)
        mRangeSeekBarView.isNotifyWhileDragging = true
        mRangeSeekBarView.setOnRangeSeekBarChangeListener(object :
            RangeSeekBarView.OnRangeSeekBarChangeListener {

            override fun onRangeSeekBarValuesChanged(
                bar: RangeSeekBarView?,
                minValue: Long,
                maxValue: Long,
                action: Int,
                isMin: Boolean,
                pressedThumb: RangeSeekBarView.Thumb?
            ) {
                mLeftProgressPos = minValue + scrollPos
                mRedProgressBarPos = mLeftProgressPos
                mRightProgressPos = maxValue + scrollPos

                when (action) {
                    MotionEvent.ACTION_DOWN -> isSeeking = false
                    MotionEvent.ACTION_MOVE -> {
                        isSeeking = true
                        seekTo(
                            (if (pressedThumb === RangeSeekBarView.Thumb.MIN) mLeftProgressPos else mRightProgressPos).toInt()
                                .toLong()
                        )
                    }

                    MotionEvent.ACTION_UP -> {
                        isSeeking = false
                        seekTo(mLeftProgressPos.toInt().toLong())

                    }

                    else -> {

                    }
                }

                mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
            }

        })
        seekBarLayout.addView(mRangeSeekBarView)
        if (mThumbsTotalCount - MAX_COUNT_RANGE > 0) {
            mAverageMsPx =
                (mDuration - MAX_SHOOT_DURATION) / (mThumbsTotalCount - MAX_COUNT_RANGE).toFloat()
        } else {
            mAverageMsPx = 0f
        }
        averagePxMs = mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos)

    }

    private fun playVideoOrPause() {

        mRedProgressBarPos = mVideoView.currentPosition.toLong()
        if (mVideoView.isPlaying()) {
            mVideoView.pause()
            pauseRedProgressAnimation()
        } else {
            mVideoView.start()
            playingRedProgressAnimation()
        }
        setPlayPauseViewIcon(mVideoView.isPlaying())
    }

    private fun setPlayPauseViewIcon(isPlaying: Boolean) {
        mPlayView?.setImageResource(if (isPlaying) R.drawable.ic_video_pause_black else R.drawable.ic_video_play_black)

    }

    private var mRedProgressAnimator: ValueAnimator? = null
    private val mAnimationHandler = Handler()
    private fun playingRedProgressAnimation() {

            pauseRedProgressAnimation()
            playingAnimation()
            mAnimationHandler.post(mAnimationRunnable)

    }


    private val mAnimationRunnable = Runnable { updateVideoProgress() }
    private fun calcScrollXDistance(): Int {
        val layoutManager = video_frames_recyclerView.getLayoutManager() as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView?.width ?: 0
        return position * itemWidth - firstVisibleChildView?.left!!
    }

    private fun updateVideoProgress() {
        val currentPosition: Long = mVideoView.currentPosition.toLong()

        if (currentPosition >= mRightProgressPos) {
            mRedProgressBarPos = mLeftProgressPos
            pauseRedProgressAnimation()
            onVideoPause()
        } else {
            mAnimationHandler.post(mAnimationRunnable)
        }
    }

    private fun pauseRedProgressAnimation() {
        mRedProgressIcon.clearAnimation()
        if (mRedProgressAnimator != null && mRedProgressAnimator!!.isRunning) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable)
            mRedProgressAnimator!!.cancel()
        }
    }

    fun onVideoPause() {
        if (mVideoView.isPlaying()) {
            seekTo(mLeftProgressPos) //复位
            mVideoView.pause()
            setPlayPauseViewIcon(false)
            mRedProgressIcon.visibility = View.GONE
        }
    }


    private fun playingAnimation() {
        if (mRedProgressIcon.visibility == View.GONE) {
            mRedProgressIcon.visibility = View.VISIBLE
        }
        val params = mRedProgressIcon.layoutParams as FrameLayout.LayoutParams
        val start = (RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs).toInt()
        val end = (RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs).toInt()
        mRedProgressAnimator = ValueAnimator.ofInt(start, end)
            .setDuration(mRightProgressPos - scrollPos - (mRedProgressBarPos - scrollPos))
        mRedProgressAnimator?.interpolator = LinearInterpolator()
        mRedProgressAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animation ->
            params.leftMargin = animation.animatedValue as Int
            mRedProgressIcon.layoutParams = params

        })
        mRedProgressAnimator?.start()
    }

    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var scrollX: Int = 0
                isSeeking = false
                try {
                    scrollX = calcScrollXDistance()
                } catch (e: java.lang.Exception) {
                    scrollX = 0
                }
                if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                    isOverScaledTouchSlop = false
                    return
                }
                isOverScaledTouchSlop = true

                if (scrollX == -RECYCLER_VIEW_PADDING) {
                    scrollPos = 0
                    mLeftProgressPos = mRangeSeekBarView.selectedMinValue + scrollPos
                    mRightProgressPos = mRangeSeekBarView.selectedMaxValue + scrollPos

                    mRedProgressBarPos = mLeftProgressPos
                } else {
                    isSeeking = true
                    scrollPos =
                        ((mAverageMsPx * (RECYCLER_VIEW_PADDING + scrollX) / THUMB_WIDTH).toLong())
                    mLeftProgressPos = mRangeSeekBarView.selectedMinValue + scrollPos
                    mRightProgressPos = mRangeSeekBarView.selectedMaxValue + scrollPos

                    mRedProgressBarPos = mLeftProgressPos
                    if (mVideoView.isPlaying()) {
                        mVideoView.pause()
                        setPlayPauseViewIcon(false)
                    }
                    mRedProgressIcon.setVisibility(View.GONE)
                    seekTo(mLeftProgressPos)
                    mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
                    mRangeSeekBarView.invalidate()
                }
                lastScrollX = scrollX
            }
        }

    private fun seekTo(msec: Long) {
        this.mVideoView.seekTo(msec.toInt())

    }


}

class SpacesItemDecoration2(private val space: Int, private val thumbnailsCount: Int) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.left = space
            outRect.right = 0
        } else if (thumbnailsCount > 10 && position == thumbnailsCount - 1) {
            outRect.left = 0
            outRect.right = space
        } else {
            outRect.left = 0
            outRect.right = 0
        }
    }
}