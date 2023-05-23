package com.suraj854.trimmodule.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.adapters.MediaAttachmentAdapter
import com.suraj854.trimmodule.interfaces.MediaItemClickListener
import com.suraj854.trimmodule.interfaces.TrimLayoutListener
import com.suraj854.trimmodule.interfaces.VideoPreparedListener
import com.suraj854.trimmodule.interfaces.ViewPager2Listener
import com.suraj854.trimmodule.models.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaAttachmentFragment : Fragment(), MediaItemClickListener, VideoPreparedListener,
    ViewPager2Listener {
    private lateinit var mediaItemViewPager2: ViewPager2
    private lateinit var mediaAttachmentAdapter: MediaAttachmentAdapter
    private var mediaList = mutableListOf<MediaItem>()
    private var trimLayoutListener: TrimLayoutListener? = null // TrimLayoutListener reference


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun ViewPager2.getVideoViewAtPosition(position: Int): VideoView? {
        val recyclerView = this.getChildAt(0) as? RecyclerView
        val viewHolder =
            recyclerView?.findViewHolderForAdapterPosition(position) as? MediaAttachmentAdapter.MediaAttachmentViewHolder
        return viewHolder?.mediaItemVideoView
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
        mediaAttachmentAdapter = MediaAttachmentAdapter(requireContext(), mediaList, this, this)
        mediaItemViewPager2.adapter = mediaAttachmentAdapter
        mediaItemViewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                CoroutineScope(Dispatchers.Main).launch {

                    val mediaItem = mediaList.get(position)

                    if (mediaItem.isVideo) {
                        onTrimButtonClick()
                        onMediaItemListener(mediaItem)

                    } else {
                        onNonVideoItemClick()
                    }
                    onScrollPosition(position, mediaItem)

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

    override suspend fun onVideoPrepared(videoView: VideoView) {

        trimLayoutListener?.trimVideoVideoListener(videoView)
    }

    override suspend fun onMediaItemListener(mediaItem: MediaItem) {
        trimLayoutListener?.trimMediaItemListener(mediaItem)
    }

    override fun onScrollPosition(position: Int, mediaItem: MediaItem) {
        trimLayoutListener?.onMediaChange(position,mediaItem)
    }
}