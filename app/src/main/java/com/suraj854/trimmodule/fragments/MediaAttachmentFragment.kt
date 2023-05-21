package com.suraj854.trimmodule.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
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
        mediaAttachmentAdapter = MediaAttachmentAdapter(requireContext(), mediaList, this)
        mediaItemViewPager2.adapter = mediaAttachmentAdapter
        mediaItemViewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val mediaItem = mediaList.get(position)
                if (mediaItem.isVideo) {
                    onTrimButtonClick()
                } else {
                    onNonVideoItemClick()
                }
            }
        })

        return item

    }


    fun setTrimLayoutListener(listener: TrimLayoutListener) {
        trimLayoutListener = listener
    }

    override fun onTrimButtonClick() {

        trimLayoutListener?.showTrimLayout()

    }

    override fun onNonVideoItemClick() {

        trimLayoutListener?.hideTrimLayout()

    }

    fun addMediaItem(item: MediaItem) {
        mediaList.add(item)
        mediaAttachmentAdapter.notifyDataSetChanged()


    }
}