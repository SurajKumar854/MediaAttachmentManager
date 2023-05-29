package com.suraj854.trimmodule

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.viewpager2.widget.ViewPager2
import com.otaliastudios.transcoder.source.UriDataSource
import com.papayacoders.customvideocropper.video_trimmer.view.TimeLineView
import com.suraj854.trimmodule.adapters.MediaAttachmentAdapter
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import com.suraj854.trimmodule.utilis.MediaTypeUtils
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
import com.suraj854.trimmodule.viewmodels.MediaAttachmentViewModel
import com.suraj854.trimmodule.widget.papayacoder.BackgroundExecutor
import com.suraj854.trimmodule.widget.papayacoder.TrimVideoUtils
import com.suraj854.trimmodule.widget.papayacoder.interfaces.OnRangeSeekBarListener
import com.suraj854.trimmodule.widget.papayacoder.interfaces.VideoTrimmingListener
import com.suraj854.trimmodule.widget.papayacoder.view.RangeSeekBarView
import com.suraj854.videotrimmerview.utilis.BaseUtils
import com.suraj854.videotrimmerview.utilis.UnitConverter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class VideoTrimmerActivity : AppCompatActivity(), TrimLayoutListener, VideoTrimmingListener {

    private val mediaAttachmentViewModel: MediaAttachmentViewModel by viewModels()
    lateinit var mrangeSeekbar: RangeSeekBarView
    lateinit var mTimeLineView: TimeLineView
    lateinit var startDuration: TextView
    lateinit var endDurationText: TextView
    lateinit var trimmingContainer: FrameLayout
    private lateinit var timeLineTextContainer: FrameLayout
    private lateinit var mediaItemViewPager2: ViewPager2
    private lateinit var mediaAttachmentAdapter: MediaAttachmentAdapter
    private var mediaList = mutableListOf<MediaItemEntity>()
    lateinit var addMediaBtn: Button
    lateinit var mVideoView: VideoView
    private var timeVideo = 0
    private lateinit var trimTimeRangeTextView: TextView

    private var maxDurationInMs: Int = 0
    private var startPosition = 0
    private var endPosition = 0
    private var duration = 0
    private var videoTrimmingListener: VideoTrimmingListener? = null
    private lateinit var progressDialog: Dialog
    private var dstFile: File? = null
    private var currentPagePostion = 0
    var parentFolder: File? = null
    private var mediaItemJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trimmer)
        MediaTypeUtils.initialize(applicationContext)

        parentFolder = getExternalFilesDir(null)!!
        parentFolder!!.mkdirs()
        VideoTrimmerUtil.initialize(this)

        mediaItemViewPager2 = findViewById(R.id.mediaAttachmentVP)
        mediaAttachmentAdapter = MediaAttachmentAdapter(this, mediaList, this, this)
        mediaItemViewPager2.adapter = mediaAttachmentAdapter
        trimmingContainer = findViewById(R.id.trimmingContainer)
        timeLineTextContainer = findViewById(R.id.timeLineTextContainer)
        mediaItemViewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Toast.makeText(this@VideoTrimmerActivity, "$position", Toast.LENGTH_SHORT).show()

                mediaItemJob = CoroutineScope(Dispatchers.Default).launch {
                    mediaAttachmentViewModel.getMediaItem(position).collect {

                        mediaItem = it


                        this@VideoTrimmerActivity.runOnUiThread(java.lang.Runnable {

                            if (it.isVideo) {
                                trimmingContainer.visibility = View.VISIBLE
                                timeLineTextContainer.visibility = View.VISIBLE
                                setTimeLine(it)

                                mrangeSeekbar.setThumbPos(0, it.lastLeftThumbPosition)
                                mrangeSeekbar.setThumbPos(1, it.lastRightThumbPosition)

                                onRangeUpdated(
                                    it.leftProgress.toInt(), it.rightProgress.toInt()
                                )
                            } else {
                                trimmingContainer.visibility = View.GONE
                                timeLineTextContainer.visibility = View.GONE

                            }


                        })


                    }
                }


            }

        })

        /*    mediaAttachmentViewModel.safetyIncidentSite.observe(this, Observer {
                Toast.makeText(this, "ssss", Toast.LENGTH_SHORT).show()
            })*/
        mediaAttachmentViewModel.cleanAttachment()
        lifecycle.coroutineScope.launch {
            mediaAttachmentViewModel.mediaItems.collect {

                mediaList.clear()
                mediaList.addAll(it)
                mediaAttachmentAdapter.notifyDataSetChanged()

            }
        }

        mTimeLineView = findViewById(R.id.timeLineView)
        mrangeSeekbar = findViewById(R.id.rangeSeekBarView)
        addMediaBtn = findViewById(R.id.addMediaBtn)
        trimTimeRangeTextView = findViewById(R.id.trimTimeRangeTextView)
        startDuration = findViewById(R.id.startTimeTextView)
        endDurationText =
            findViewById(R.id.endTimeLineTextView)/* fragment.setTrimLayoutListener(this)*/
        videoTrimmingListener = this
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_loading)
        progressDialog.setCancelable(false)


        mPostBtn = findViewById(R.id.mPostBtn)
        mrangeSeekbar.initMaxWidth()
        setMaxDurationInMs(10 * 10000)
        mrangeSeekbar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

                // Do nothing
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {

                Log.e("onSeeksss", "value $index  float ${value * 9.45}")

                onSeekThumbs(rangeSeekBarView, index, value)
            }

            override fun onSeekStart(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

            }

            override fun onSeekStop(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

            }
        })

        mPostBtn.setOnClickListener {
            if (mediaList.isEmpty()) {
                Toast.makeText(this, "Please select something", Toast.LENGTH_SHORT).show()
            } else {


                showLoadingDialog("Encoding..")
                encodeAttachments()


            }
        }

        addMediaBtn.setOnClickListener {
            if (MediaTypeUtils.checkCamStoragePer(this)) {
                openMultipleMedia()

            }
        }


    }

    private fun encodeAttachments() {
        encodeIndex = 0
        encodeAttachmentsRecursive(encodeIndex)

    }

    var encodeIndex = -1

    fun encodeAttachmentsRecursive(index: Int) {
        encodeIndex = index
        if (index >= mediaList.size) {
            showLoadingDialog("Encoding($index/${mediaList.size - 1})")
            Toast.makeText(this, "Encoded Successfully", Toast.LENGTH_SHORT).show()
            hideLoadingDialog()
            return
        }
        showLoadingDialog("Encoding($index/${mediaList.size - 1})")

        val uploadMediaAttachment = mediaList.get(encodeIndex)
        if (uploadMediaAttachment.isVideo) {
            val source = UriDataSource(this, Uri.parse(uploadMediaAttachment.path));
            val start = uploadMediaAttachment.trimFromStart
            val trimFromLeft = uploadMediaAttachment.trimFromEnd

            try {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
                dstFile = File(parentFolder, fileName)


                /*      mVideoView.pause()*/

                videoTrimmingListener!!.onTrimStarted()
                BackgroundExecutor.execute(object : BackgroundExecutor.Task(null, 100L, null) {
                    override fun execute() {
                        try {
                            TrimVideoUtils.startTrim(
                                this@VideoTrimmerActivity,
                                Uri.parse(uploadMediaAttachment.path),
                                dstFile!!,
                                uploadMediaAttachment.leftProgress,
                                uploadMediaAttachment.rightProgress,
                                uploadMediaAttachment.duration,
                                videoTrimmingListener!!
                            )
                        } catch (e: Throwable) {
                            Thread.getDefaultUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e)
                        }
                    }
                })
            } catch (e: java.lang.Exception) {

            }


        } else {
            encodeAttachmentsRecursive(encodeIndex + 1)
        }
    }

    private fun setMaxDurationInMs(maxDurationInMs: Int) {

        this.maxDurationInMs = maxDurationInMs
    }

    private fun seekTo(msec: Long) {
        mediaAttachmentAdapter.seekTo(msec)


    }


    private fun showLoadingDialog(message: String) {
        progressDialog.show()
        val textMessage = progressDialog.findViewById<TextView>(R.id.textMessage)
        textMessage.text = message
    }


    private fun hideLoadingDialog() {
        progressDialog.dismiss()
    }

    private fun onSeekThumbs(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {

        when (index) {
            RangeSeekBarView.ThumbType.LEFT.index -> {
                startPosition = (mediaItem.duration * value / 100L).toInt()
                mediaItem.lastLeftThumbPosition = rangeSeekBarView.getThumbPosition(index)
                mediaAttachmentViewModel.updateMediaItem(mediaItem)

            }

            RangeSeekBarView.ThumbType.RIGHT.index -> {
                mediaItem.lastRightThumbPosition = rangeSeekBarView.getThumbPosition(index)
                endPosition = (mediaItem.duration * value / 100L).toInt()
                mediaAttachmentViewModel.updateMediaItem(mediaItem)
            }
        }
        onRangeUpdated(startPosition, endPosition)
        seekTo(startPosition.toLong())
        timeVideo = endPosition - startPosition
    }

    lateinit var mPostBtn: Button

    private fun onRangeUpdated(startTimeInMs: Int, endTimeInMs: Int) {
        val seconds = "Sec"
        mediaItem.leftProgress = startTimeInMs.toLong()
        mediaItem.rightProgress = endTimeInMs.toLong()
        mediaAttachmentViewModel.updateMediaItem(mediaItem)
        startDuration.text = "${stringForTime(startTimeInMs)} $seconds "
        endDurationText.text = " ${stringForTime(endTimeInMs)} $seconds"
    }

    @SuppressLint("NotifyDataSetChanged")
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

                        val mediaType = uri?.uri?.let { MediaTypeUtils.getMediaType(it) }
                        if (mediaType == MediaTypeUtils.MediaType.IMAGE) {
                            // Process as an image
                            val mediaItem = MediaItemEntity(
                                uri.uri.toString(),
                                0,
                                false,
                                0,
                                0,
                                0,
                                0,
                                0f,
                                UnitConverter().dpToPx(385f)
                            )

                            mediaAttachmentViewModel.insertMediaItem(mediaItem)


                            mediaAttachmentAdapter.notifyDataSetChanged()/* MediaItem(
                                     uri.uri.toString(),
                                     0,
                                     false,
                                     0,
                                     0,
                                     10000,
                                     0.0,
                                     UnitConverter().dpToPx(385f).toDouble(),
                                     0,
                                     10000
                                 )*/
                        } else if (mediaType == MediaTypeUtils.MediaType.VIDEO) {
                            // Process as a video

                            val mediaItem = MediaItemEntity(
                                uri.uri.toString(),
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())),
                                true,
                                0,
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())),
                                0,
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())),
                                0f,
                                UnitConverter().dpToPx(385f)
                            )


                            mediaAttachmentViewModel.insertMediaItem(mediaItem)
                            mediaAttachmentAdapter.notifyDataSetChanged()/*   fragment.addMediaItem(
                                   MediaItem(
                                       uri.uri.toString(),
                                       MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())),
                                       true,
                                       0,
                                       0,
                                       MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())) - 10000,
                                       1.0,
                                       UnitConverter().dpToPx(385f).toDouble(),
                                       0,
                                       MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString()))
                                   )
                               )*/
                        }

                    }

                } else if (result.data != null) {
                    val uri = result?.data?.data
                    val mediaType = uri?.let { MediaTypeUtils.getMediaType(it) }
                    if (mediaType == MediaTypeUtils.MediaType.IMAGE) {
                        // Process as an image

                        /* fragment.addMediaItem(
                             MediaItem(
                                 uri.toString(),
                                 0,
                                 false,
                                 0,
                                 0,
                                 10000,
                                 0.0,
                                 UnitConverter().dpToPx(385f).toDouble(),
                                 0,
                                 10000
                             )*/
                        val mediaItem = MediaItemEntity(
                            uri.toString(), 0, false, 0, 0, 0, 0, 0f, UnitConverter().dpToPx(385f)
                        )


                        mediaAttachmentViewModel.insertMediaItem(mediaItem)
                        mediaAttachmentAdapter.notifyDataSetChanged()

                    } else if (mediaType == MediaTypeUtils.MediaType.VIDEO) {
                        // Process as a video
                        val mediaItem = MediaItemEntity(
                            uri.toString(),
                            MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())),
                            true,
                            0,
                            MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())),
                            0,
                            MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())),
                            0f,
                            UnitConverter().dpToPx(385f)
                        )/*  AppDatabase.getAppDataBase(applicationContext)?.mediaAttachmentDao()
                              ?.insertMediaItem(
                                  mediaItem
                              )*/
                        mediaAttachmentViewModel.insertMediaItem(mediaItem)
                        mediaAttachmentAdapter.notifyDataSetChanged()/*fragment.addMediaItem(
                            MediaItem(
                                uri.toString(),
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())),
                                true,
                                0,
                                0,
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())) - 10000,
                                0.0,
                                UnitConverter().dpToPx(385f).toDouble(),
                                0,
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString()))
                            )
                        )*/
                    }
                }

            }

        }

    private fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val timeFormatter = java.util.Formatter()
        return if (hours > 0) timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
            .toString()
        else timeFormatter.format("%02d:%02d", minutes, seconds).toString()
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

    fun setTimeLine(mediaItem: MediaItemEntity) {
        BaseUtils.init(this)
        mTimeLineView.setVideo(Uri.parse(mediaItem.path), mediaItem)
        mTimeLineView.getBitmap(1080, 110)

    }

    override fun showTrimLayout() {/*      startDuration.visibility = View.VISIBLE
              endDurationText.visibility = View.VISIBLE
              mTimeLineView.visibility = View.VISIBLE
              mrangeSeekbar.visibility = View.VISIBLE*/
    }

    override fun hideTrimLayout() {/* startDuration.visibility = View.GONE
         endDurationText.visibility = View.GONE
         mTimeLineView.visibility = View.GONE
         mrangeSeekbar.visibility = View.GONE*/
    }

    lateinit var mediaItem: MediaItemEntity
    override fun onMediaChange(position: Int, mediaItem: MediaItem) {
        currentPagePostion = position


        if (mediaItem.isVideo) {


            /* setTimeLine(mediaItem)*/
            startPosition = 0
            endPosition = mediaItem.duration.toInt()/*   startPosition = 0
               endPosition = MediaTypeUtils.getVideoDuration(
                   Uri.parse(mediaItem.path)
               ).toInt() / 2 + maxDurationInMs / 2

               duration = MediaTypeUtils.getVideoDuration(
                   Uri.parse(mediaItem.path)
               ).toInt()
   *//*   mrangeSeekbar.setThumbValue(0, startPosition * 100f / duration)
               mrangeSeekbar.setThumbValue(1, endPosition * 100f / duration)*/

            /* endPosition = MediaTypeUtils.getVideoDuration(
                 Uri.parse(mediaItem.path)
             ).toInt()*/


            /* mrangeSeekbar.setThumbPos(0, mediaItem.lastLeftThumbPosition.toFloat())
             mrangeSeekbar.setThumbPos(1, mediaItem.lastRightThumbPosition.toFloat())*/


            //    mrangeSeekbar.restoreThumbValues()*/
            Log.e(
                "onMediaChange",
                "   lastRightThumbPosition ${mediaItem.lastRightThumbPosition.toFloat()}   lastLeftThumbPosition ${mediaItem.lastLeftThumbPosition.toFloat()}  endPosition ${
                    MediaTypeUtils.getVideoDuration(
                        Uri.parse(mediaItem.path.toString())
                    )
                } $"
            )

            // lastRightThumbPosition 1347.5   lastLeftThumbPosition 1.0  endPosition 52198 $


            mrangeSeekbar.setThumbPos(0, 600f)
            mrangeSeekbar.setThumbPos(1, 1340f)
            mrangeSeekbar.setThumbValue(0, 60f)
            mrangeSeekbar.setThumbValue(1, 80f)
            onRangeUpdated(
                mediaItem.leftProgress.toInt(), mediaItem.rightProgress.toInt()
            )

        }
        //  mrangeSeekbar.initMaxWidth()
    }

    override fun trimMediaItemListener(mediaItem: MediaItem) {

    }

    override fun trimVideoVideoListener(video: VideoView) {
        this.mVideoView = video
        this.mVideoView.requestFocus()


        mrangeSeekbar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

                // Do nothing
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {

                Log.e("onSeeksss", "value $index  float ${value * 9.45}")
                onSeekThumbs(rangeSeekBarView, index, value)
            }

            override fun onSeekStart(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

            }

            override fun onSeekStop(
                rangeSeekBarView: RangeSeekBarView, index: Int, value: Float
            ) {

            }
        })


    }

    private fun setSeekBarPosition() {/*  if (duration >= maxDurationInMs) {
              startPosition = duration / 2 - maxDurationInMs / 2
              endPosition = duration / 2 + maxDurationInMs / 2

              mrangeSeekbar.setThumbValue(0, startPosition * 100f / duration)
              mrangeSeekbar.setThumbValue(1, endPosition * 100f / duration)
          } else {
              startPosition = 0
              endPosition = duration


          }

          //  setProgressBarPosition(startPosition)
          mVideoView.seekTo(startPosition)
          timeVideo = duration
          *//*mrangeSeekbar.initMaxWidth()*/

    }

    override fun onVideoPrepared() {

    }

    override fun onTrimStarted() {

    }

    override fun onFinishedTrimming(uri: Uri?) {
        Toast.makeText(applicationContext, "Finished", Toast.LENGTH_SHORT).show()
        encodeAttachmentsRecursive(encodeIndex + 1)
        hideLoadingDialog()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        Toast.makeText(applicationContext, "onErrorWhileViewingVideo", Toast.LENGTH_SHORT).show()

    }

}