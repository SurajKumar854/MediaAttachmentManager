package com.suraj854.trimmodule.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.adapters.MediaAttachmentAdapter
import com.suraj854.trimmodule.fragments.MediaAttachmentFragment
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem

class MediaAttachmentActivity : AppCompatActivity(), TrimLayoutListener {
    lateinit var fragmentContainer: FrameLayout
    lateinit var trimLL: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_attachment)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        val fragment = MediaAttachmentFragment()
        fragment.setTrimLayoutListener(this)
        trimLL = findViewById(R.id.trimLL)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()

    }

    override fun showTrimLayout() {
        trimLL.visibility = View.VISIBLE

    }

    override fun hideTrimLayout() {

        trimLL.visibility = View.GONE

    }
}