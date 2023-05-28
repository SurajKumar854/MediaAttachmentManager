package com.suraj854.trimmodule.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
var currentMediaPage = 0
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

                currentMediaPage = position

                Toast.makeText(requireContext(), "$position", Toast.LENGTH_SHORT).show()
                val mediaItem = mediaList.get(position)

                if (mediaItem.isVideo) {
                    onTrimButtonClick()
                    onMediaItemListener(mediaItem)

                } else {
                    onNonVideoItemClick()
                }


                onScrollPosition(position, mediaItem)
            }


            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)



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

    fun getMediaItem(position: Int): MediaItem {
        return mediaList.get(position)


    }

    fun updateThumbPositions(
        position: Int,
        left: Double,
        right: Double,
        leftProgress: Long,
        rightProgress: Long
    ) {
        val item = mediaList.get(position)
        item.lastLeftThumbPosition = left
        item.lastRightThumbPosition = right
        item.leftProgress = leftProgress
        item.rightProgress = rightProgress
        item.trimFromStart = leftProgress
        item.trimFromEnd = item.duration - rightProgress


    }
    fun updateThumbPositionTimeValues(
        position: Int,
        leftProgress: Long,
        rightProgress: Long
    ) {
        val item = mediaList.get(position)
        item.leftProgress = leftProgress
        item.rightProgress = rightProgress
        item.trimFromStart = leftProgress
        item.trimFromEnd = item.duration - rightProgress
        Log.e("updateThumbPositionTimeValues","$leftProgress  $rightProgress")


    }


    fun updateLastFrameScrollPosition(
        position: Int,
        frameIndex: Int,
    ) {
        var mpostion = position
        val item = mediaList.get(mpostion)
        item.frameIndex = frameIndex

    }


    fun getMediaList(): List<MediaItem> {

        return mediaList
    }

    override fun onVideoPrepared(videoView: VideoView) {

        trimLayoutListener?.trimVideoVideoListener(videoView)
    }

    override  fun onMediaItemListener(mediaItem: MediaItem) {
        trimLayoutListener?.trimMediaItemListener(mediaItem)
    }

    override fun onScrollPosition(position: Int, mediaItem: MediaItem) {
        trimLayoutListener?.onMediaChange(position, mediaItem)
    }

    override fun mediaPlackHandler(position: Int, mediaItem: MediaItem) {
    }
}