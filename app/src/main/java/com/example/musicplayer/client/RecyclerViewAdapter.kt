package com.example.musicplayer.client

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.track_item.view.*

class RecyclerViewAdapter(
    private val onTrackClickListener: OnTrackClickListener
) : RecyclerView.Adapter<RecyclerViewAdapter.MusicPlayerHolder>() {

    private var playList =  listOf<MediaBrowserCompat.MediaItem>()
    private var selectedItem = 0

    fun setPlayList(playList : List<MediaBrowserCompat.MediaItem>){
        this.playList = playList
        notifyDataSetChanged()
    }

    fun selectItem(position: Int){
        Log.d("mmm", "RecyclerViewAdapter :  selectItem --  ")
        notifyItemChanged(selectedItem)
        selectedItem = position
        notifyItemChanged(selectedItem)
    }

    interface OnTrackClickListener {
        fun onTrackClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MusicPlayerHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.track_item,
                parent,
                false
            ), onTrackClickListener
        )

    override fun getItemCount() = playList.size

    override fun onBindViewHolder(holder: MusicPlayerHolder, position: Int) {

        holder.itemView.isSelected = selectedItem == position

        holder.onBind(playList[position], position)
    }

    inner class MusicPlayerHolder(
        private val view: View,
        private val onTrackClickListener: OnTrackClickListener
    ) : RecyclerView.ViewHolder(view) {
        fun onBind(mediaItem: MediaBrowserCompat.MediaItem, position: Int) {

            val totalSeconds = mediaItem.description.subtitle.toString().toInt() / 1000
            val minutes = totalSeconds / 60
            val seconds = (totalSeconds - minutes * 60)
            view.textViewDuration.text =  "$minutes:$seconds"
            view.textViewTrack.text =  mediaItem.description.title

            view.setOnClickListener {
                onTrackClickListener.onTrackClick(position)
            }
        }
    }
}