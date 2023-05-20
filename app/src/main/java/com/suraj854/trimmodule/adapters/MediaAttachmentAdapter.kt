package com.suraj854.trimmodule.adapters


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.interfaces.MediaItemClickListener
import com.suraj854.trimmodule.models.MediaItem


class MediaAttachmentAdapter(
    private val context: Context,
    private val mediaList: List<MediaItem>,
    private val mediaItemClickListener: MediaItemClickListener
) :
    RecyclerView.Adapter<MediaAttachmentAdapter.MediaAttachmentViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaAttachmentAdapter.MediaAttachmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.media_item_attachment_fragment, parent, false)
        return MediaAttachmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MediaAttachmentAdapter.MediaAttachmentViewHolder,
        position: Int
    ) {


        val mediaItem = mediaList.get(position)
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun getItemViewType(position: Int): Int {
        val mediaItem = mediaList.get(position)
        if (mediaItem.isVideo) {
            mediaItemClickListener.onTrimButtonClick()
        } else {
            mediaItemClickListener.onNonVideoItemClick()
        }
        return super.getItemViewType(position)
    }

    inner class MediaAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mediaItemTxt: TextView = itemView.findViewById(R.id.mediaTypeTxt)


        fun bind(mediaItem: MediaItem) {
            mediaItemTxt.text = mediaItem.path


        }

    }
}