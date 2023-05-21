package com.suraj854.trimmodule.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.fragments.MediaAttachmentFragment
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem
import com.suraj854.trimmodule.utilis.MediaTypeUtils
import com.suraj854.trimmodule.utilis.MediaTypeUtils.MediaUtils.checkCamStoragePer
import com.suraj854.trimmodule.utilis.MediaTypeUtils.MediaUtils.getMediaType

class MediaAttachmentActivity : AppCompatActivity(), TrimLayoutListener {
    lateinit var fragmentContainer: FrameLayout
    lateinit var trimLL: LinearLayout
    lateinit var addMediaBtn: Button
    val fragment: MediaAttachmentFragment = MediaAttachmentFragment()

    init {

    }

    val addMediaChooserResult =
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
                            Log.e("Image",uri.uri.toString())
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_attachment)
        MediaTypeUtils.initialize(applicationContext)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        addMediaBtn = findViewById(R.id.addMediaBtn)


        fragment.setTrimLayoutListener(this)
        trimLL = findViewById(R.id.trimLL)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()

        addMediaBtn.setOnClickListener {
            if (checkCamStoragePer(this)) {
                openMultipleMedia()
            }

        }


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

    override fun showTrimLayout() {
        trimLL.visibility = View.VISIBLE

    }

    override fun hideTrimLayout() {

        trimLL.visibility = View.GONE

    }


}