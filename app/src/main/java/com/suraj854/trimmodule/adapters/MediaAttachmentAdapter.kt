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
import com.suraj854.trimmodule.VideoTrimmerActivity
import com.suraj854.trimmodule.room.dao.MediaItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MediaAttachmentAdapter(
    private val context: Context,
    private var mediaList: MutableList<MediaItemEntity>,
    private val mediaItemClickListener: VideoTrimmerActivity,
    private val videoPreparedListener: VideoTrimmerActivity
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
        this.holder = holder

        val mediaItem = mediaList.get(position)
        holder.bind(mediaItem)
    }

    lateinit var holder: MediaAttachmentViewHolder
    fun seekTo(ms: Long) {

        holder.mediaItemVideoView.seekTo(ms.toInt())

        notifyDataSetChanged()
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


        }

        fun bind(mediaItem: MediaItemEntity) {

            if (mediaItem.isVideo) {

                mediaItemImageView.visibility = View.GONE
                mediaItemVideoView.visibility = View.VISIBLE
                mediaItemVideoPlayBtnView.visibility = View.VISIBLE
                mediaItemVideoView.setVideoURI(Uri.parse(mediaItem.path))
                mediaItemVideoView.seekTo(1 + mediaItem.leftProgress.toInt())

                mediaItemVideoPlayBtnView.setOnClickListener {
                    if (!mediaItemVideoView.isPlaying) {
                        mediaItemVideoView.start()
                      /*  CoroutineScope(Dispatchers.Main).launch {
                            delay(mediaItem.rightProgress)
                            mediaItemVideoView.pause()
                            mediaItemVideoView.seekTo(mediaItem.rightProgress.toInt())
                        }
                      */
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