package com.suraj854.trimmodule.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.adapters.MediaAttachmentAdapter
import com.suraj854.trimmodule.interfaces.MediaItemClickListener
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.models.MediaItem

class MediaAttachmentFragment : Fragment(), MediaItemClickListener {
    private lateinit var mediaItemViewPager2: ViewPager2
    private lateinit var mediaAttachmentAdapter: MediaAttachmentAdapter
    private var mediaList = mutableListOf<MediaItem>()
    private var trimLayoutListener: TrimLayoutListener? = null // TrimLayoutListener reference


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val item = inflater.inflate(
            R.layout.media_attachment_fragment, container, false
        )
        mediaItemViewPager2 = item.findViewById(R.id.mediaAttachmentVP)
        mediaList.add(MediaItem("image1.jpg", false))
        mediaList.add(MediaItem("video1.mp4", true))
        mediaAttachmentAdapter = MediaAttachmentAdapter(requireContext(), mediaList, this)
        mediaItemViewPager2.adapter = mediaAttachmentAdapter

        return item

    }

    fun setTrimLayoutListener(listener: TrimLayoutListener) {
        trimLayoutListener = listener
    }

    override fun onTrimButtonClick() {
        Log.e("video", "Clicked")
        trimLayoutListener?.showTrimLayout()

    }

    override fun onNonVideoItemClick() {
        Log.e("image", "Clicked")
        trimLayoutListener?.hideTrimLayout()

    }
}