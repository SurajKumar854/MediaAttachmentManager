package com.suraj854.trimmodule.adapters


import android.content.Context
import android.media.MediaPlayer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

    override fun onViewAttachedToWindow(holder: MediaAttachmentViewHolder) {
        super.onViewAttachedToWindow(holder)

        onBindViewHolder(holder, holder.getAdapterPosition());

    }

    inner class MediaAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaItemVideoView: VideoView = itemView.findViewById(R.id.videView)
        val mediaItemVideoPlayBtnView: ImageView = itemView.findViewById(R.id.playbackBtn)
        val mediaItemImageView: ImageView = itemView.findViewById(R.id.imagePreview)

        init {


            mediaItemVideoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                CoroutineScope(Dispatchers.Main).launch {
                    videoPreparedListener.onVideoPrepared(mediaItemVideoView)
                }


            }

        }

        fun bind(mediaItem: MediaItem) {

            if (mediaItem.isVideo) {

                mediaItemImageView.visibility = View.GONE
                mediaItemVideoView.visibility = View.VISIBLE
                mediaItemVideoPlayBtnView.visibility = View.VISIBLE
                mediaItemVideoView.setVideoURI(Uri.parse(mediaItem.path))
                mediaItemVideoView.seekTo(1 + mediaItem.leftProgress.toInt())
                mediaItemVideoPlayBtnView.setOnClickListener {
                    if (!mediaItemVideoView.isPlaying) {
                        mediaItemVideoView.start()
                        mediaItemVideoPlayBtnView.setImageResource(R.drawable.ic_video_pause_black)

                    } else {
                        mediaItemVideoView.pause()
                        mediaItemVideoPlayBtnView.setImageResource(R.drawable.play)
                    }
                }


            } else {


                mediaItemVideoView.visibility = View.GONE
                mediaItemVideoPlayBtnView.visibility = View.GONE
                mediaItemImageView.visibility = View.VISIBLE

                mediaItemImageView.setImageURI(Uri.parse(mediaItem.path))
            }


        }

    }
}