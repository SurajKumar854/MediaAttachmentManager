package com.suraj854.trimmodule.adapters


import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.interfaces.MediaItemClickListener
import com.suraj854.trimmodule.interfaces.VideoPreparedListener
import com.suraj854.trimmodule.models.MediaItem


class MediaAttachmentAdapter(
    private val context: Context,
    private val mediaList: List<MediaItem>,
    private val mediaItemClickListener: MediaItemClickListener,
    private val videoPreparedListener: VideoPreparedListener
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

        return super.getItemViewType(position)
    }


    inner class MediaAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaItemVideoView: VideoView = itemView.findViewById(R.id.videView)
        val mediaItemImageView: ImageView = itemView.findViewById(R.id.imagePreview)

        init {

            mediaItemVideoView.setOnPreparedListener { mediaPlayer ->
                videoPreparedListener.onVideoPrepared(mediaItemVideoView)

            }

        }

        fun bind(mediaItem: MediaItem) {


            if (mediaItem.isVideo) {
                mediaItemVideoView.setVideoURI(Uri.parse(mediaItem.path))
                mediaItemVideoView.visibility = View.VISIBLE

            } else {

                mediaItemImageView.setImageURI(Uri.parse(mediaItem.path))
                mediaItemVideoView.visibility = View.GONE
            }


        }

    }
}