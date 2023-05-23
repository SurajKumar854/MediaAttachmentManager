package com.suraj854.trimmodule.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.suraj854.trimmodule.R


class VideoTrimmerAdapter(private val context: Context) :
    RecyclerView.Adapter<VideoTrimmerAdapter.TrimmerViewHolder>() {
    private val mBitmaps: MutableList<Bitmap> = ArrayList()
    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrimmerViewHolder {
        return TrimmerViewHolder(mInflater.inflate(R.layout.video_thumb_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: TrimmerViewHolder, position: Int) {
        holder.thumbImageView.setImageBitmap(mBitmaps[position])
    }


    override fun getItemCount(): Int {
        return mBitmaps.size
    }

    var count = 0

    fun addBitmaps(bitmap: Bitmap) {
        mBitmaps.add(bitmap)
        Log.e("Bitmap-Loaded addBitmaps-Previois Size", "Bitmap ${mBitmaps.size}")
        Log.e("Bitmap-Loaded", "Bitmap ${count++}")
        Log.e("Surajssss","Bitmap")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearBitmapsList() {
        Log.e("Bitmap-Loaded-Previois Size", "Bitmap ${mBitmaps.size}")
        mBitmaps.clear()
        count = 0
       this.notifyDataSetChanged()
    }

    class TrimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbImageView: ImageView = itemView.findViewById(R.id.thumb)
    }


}