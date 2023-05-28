package com.suraj854.trimmodule

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
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.transcoder.source.TrimDataSource
import com.otaliastudios.transcoder.source.UriDataSource
import com.papayacoders.customvideocropper.video_trimmer.view.TimeLineView
import com.suraj854.trimmodule.fragments.MediaAttachmentFragment
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.utilis.MediaTypeUtils
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
import com.suraj854.trimmodule.widget.papayacoder.BackgroundExecutor
import com.suraj854.trimmodule.widget.papayacoder.TrimVideoUtils
import com.suraj854.trimmodule.widget.papayacoder.interfaces.OnRangeSeekBarListener
import com.suraj854.trimmodule.widget.papayacoder.interfaces.VideoTrimmingListener
import com.suraj854.trimmodule.widget.papayacoder.view.RangeSeekBarView
import com.suraj854.videotrimmerview.utilis.BaseUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoTrimmerActivity : AppCompatActivity(), TrimLayoutListener, VideoTrimmingListener {
    lateinit var mrangeSeekbar: RangeSeekBarView
    lateinit var mTimeLineView: TimeLineView
    lateinit var startDuration: TextView
    lateinit var endDurationText: TextView
    lateinit var fragmentContainer: FrameLayout
    private val fragment: MediaAttachmentFragment = MediaAttachmentFragment()
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
    var parentFolder : File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trimmer)
        MediaTypeUtils.initialize(applicationContext)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        parentFolder = getExternalFilesDir(null)!!
        parentFolder!!.mkdirs()
        VideoTrimmerUtil.initialize(this)

        mTimeLineView = findViewById(R.id.timeLineView)
        mrangeSeekbar = findViewById(R.id.rangeSeekBarView)
        addMediaBtn = findViewById(R.id.addMediaBtn)
        trimTimeRangeTextView = findViewById(R.id.trimTimeRangeTextView)
        startDuration = findViewById(R.id.startTimeTextView)
        endDurationText = findViewById(R.id.endTimeLineTextView)
        fragment.setTrimLayoutListener(this)
        videoTrimmingListener = this
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_loading)
        progressDialog.setCancelable(false)


        mPostBtn = findViewById(R.id.mPostBtn)
        setMaxDurationInMs(10 * 10000)
        mrangeSeekbar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {

                // Do nothing
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {

                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {

            }

            override fun onSeekStop(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {

            }
        })

        mPostBtn.setOnClickListener {
            if (fragment.getMediaList().isEmpty()) {
                Toast.makeText(this, "Please select something", Toast.LENGTH_SHORT).show()
            } else {


                showLoadingDialog("Encoding..")
                encodeAttachments()


            }
        }
        setMaxDurationInMs(10 * 10000)
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
        if (index >= fragment.getMediaList().size) {
            showLoadingDialog("Encoding($index/${fragment.getMediaList().size - 1})")
            Toast.makeText(this, "Encoded Successfully", Toast.LENGTH_SHORT).show()
            hideLoadingDialog()
            return
        }
        showLoadingDialog("Encoding($index/${fragment.getMediaList().size - 1})")

        val uploadMediaAttachment = fragment.getMediaList().get(encodeIndex)
        if (uploadMediaAttachment.isVideo) {
            val source = UriDataSource(this, Uri.parse(uploadMediaAttachment.path));
            val start = uploadMediaAttachment.trimFromStart
            val trimFromLeft = uploadMediaAttachment.trimFromEnd

            try {
                val trim = TrimDataSource(
                    source, (start * 1000).toLong(), (trimFromLeft * 1000).toLong()
                );
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val outputName = "trimmedVideo_$timeStamp.mp4"
                val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
                dstFile = File(parentFolder, fileName)



                mVideoView.pause()

                videoTrimmingListener!!.onTrimStarted()
                Log.e("sfasfsafasaf","${uploadMediaAttachment.trimFromStart} / ${uploadMediaAttachment.trimFromEnd}")
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


            /* if (!folder.exists()) {
                 folder.mkdirs()
             }*/


        } else {
            encodeAttachmentsRecursive(encodeIndex + 1)
        }
    }

    private fun setMaxDurationInMs(maxDurationInMs: Int) {
        this.maxDurationInMs = 0
        this.maxDurationInMs = maxDurationInMs
    }

    private fun seekTo(msec: Long) {
        this.mVideoView.seekTo(msec.toInt())

    }


    private fun showLoadingDialog(message: String) {
        progressDialog.show()
        val textMessage = progressDialog.findViewById<TextView>(R.id.textMessage)
        textMessage.text = message // Update the text message as needed
    }


    private fun hideLoadingDialog() {
        progressDialog.dismiss()
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        Toast.makeText(this, "${value.toDouble()}", Toast.LENGTH_SHORT).show()
        when (index) {
            RangeSeekBarView.ThumbType.LEFT.index -> {
                fragment.saveLeftRangeSeekBarPosition(currentPagePostion, value.toDouble())
                startPosition = (duration * value / 100L).toInt()

            }

            RangeSeekBarView.ThumbType.RIGHT.index -> {
                fragment.saveRightRangeSeekBarPosition(currentPagePostion, value.toDouble())
                endPosition = (duration * value / 100L).toInt()
            }
        }
//        Log.d("SHUBH", "onSeekThumbs: $startPosition  $endPosition")
        // setProgressBarPosition(startPosition)

        onRangeUpdated(startPosition, endPosition)
        seekTo(startPosition.toLong())
        timeVideo = endPosition - startPosition
    }

    lateinit var mPostBtn: Button

    private fun onRangeUpdated(startTimeInMs: Int, endTimeInMs: Int) {
        val seconds = "Sec"

        startDuration.text = "${stringForTime(startTimeInMs)} $seconds "
        endDurationText.text = " ${stringForTime(endTimeInMs)} $seconds"
        fragment.updateThumbPositionTimeValues(
            currentPagePostion,
            startTimeInMs.toLong(),
            endTimeInMs.toLong()
        )
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

                        val mediaType = uri?.uri?.let { MediaTypeUtils.getMediaType(it) }
                        if (mediaType == MediaTypeUtils.MediaType.IMAGE) {
                            // Process as an image

                            fragment.addMediaItem(
                                MediaItem(
                                    uri.uri.toString(), 0, false, 0, 0, 10000, 0.0, 1.0, 0, 10000
                                )
                            )
                        } else if (mediaType == MediaTypeUtils.MediaType.VIDEO) {
                            // Process as a video
                            fragment.addMediaItem(
                                MediaItem(
                                    uri.uri.toString(),
                                    MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())),
                                    true,
                                    0,
                                    0,
                                    MediaTypeUtils.getVideoDuration(Uri.parse(uri.uri.toString())) - 10000,
                                    1.0,
                                    1010.00,
                                    0,
                                    10000
                                )
                            )
                        }

                    }

                } else if (result.data != null) {
                    val uri = result?.data?.data
                    val mediaType = uri?.let { MediaTypeUtils.getMediaType(it) }
                    if (mediaType == MediaTypeUtils.MediaType.IMAGE) {
                        // Process as an image

                        fragment.addMediaItem(
                            MediaItem(
                                uri.toString(), 0, false, 0, 0, 10000, 0.0, 1.0, 0, 10000
                            )
                        )
                    } else if (mediaType == MediaTypeUtils.MediaType.VIDEO) {
                        // Process as a video
                        fragment.addMediaItem(
                            MediaItem(
                                uri.toString(),
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())),
                                true,
                                0,
                                0,
                                MediaTypeUtils.getVideoDuration(Uri.parse(uri.toString())) - 10000,
                                0.0,
                                1010.00,
                                0,
                                10000
                            )
                        )
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

    fun setTimeLine(mediaItem: MediaItem) {
        BaseUtils.init(this)
        mTimeLineView.setVideo(Uri.parse(mediaItem.path), mediaItem)
        mTimeLineView.getBitmap(1080, 110)

    }

    override fun showTrimLayout() {
        startDuration.visibility = View.VISIBLE
        endDurationText.visibility = View.VISIBLE
        mTimeLineView.visibility = View.VISIBLE
        mrangeSeekbar.visibility = View.VISIBLE
    }

    override fun hideTrimLayout() {
        startDuration.visibility = View.GONE
        endDurationText.visibility = View.GONE
        mTimeLineView.visibility = View.GONE
        mrangeSeekbar.visibility = View.GONE
    }
    lateinit var mediaItem: MediaItem
    override fun onMediaChange(position: Int, mediaItem: MediaItem) {
        currentPagePostion = position

        this.mediaItem = mediaItem
        if (mediaItem.isVideo) {
            //  setMaxDurationInMs(10 * 10000)

            setTimeLine(mediaItem)
            startPosition = mediaItem.leftProgress.toInt()
            endPosition = mediaItem.duration.toInt()


            //    mrangeSeekbar.restoreThumbValues()*/


            onRangeUpdated(mediaItem.leftProgress.toInt(), mediaItem.duration.toInt())

        }
        mrangeSeekbar.initMaxWidth()
    }

    override fun trimMediaItemListener(mediaItem: MediaItem) {

    }

    override fun trimVideoVideoListener(video: VideoView) {
        this.mVideoView = video
        this.mVideoView.requestFocus()
        duration = video.duration
        //  mrangeSeekbar.setThumbValue(0, 30f)
        if (this.mediaItem != null) {
            /* Toast.makeText(
                 this,
                 "${this.mediaItem.lastLeftThumbPosition.toFloat()}",
                 Toast.LENGTH_SHORT
             ).show()*/


        }
    }

    private fun setSeekBarPosition() {
        if (duration >= maxDurationInMs) {
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
        /*mrangeSeekbar.initMaxWidth()*/

    }

    override fun onVideoPrepared() {

    }

    override fun onTrimStarted() {

    }

    override fun onFinishedTrimming(uri: Uri?) {

        encodeAttachmentsRecursive(encodeIndex + 1)
        hideLoadingDialog()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        Toast.makeText(applicationContext, "onErrorWhileViewingVideo", Toast.LENGTH_SHORT).show()

    }

}