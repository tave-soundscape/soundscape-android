package com.mobile.soundscape.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mobile.soundscape.R
import com.mobile.soundscape.result.MusicModel

class HomeHistoryDetailAdapter(
    private val songList: List<MusicModel>
) : RecyclerView.Adapter<HomeHistoryDetailAdapter.SongViewHolder>() {

    override fun getItemCount(): Int = songList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // 노래 아이템 레이아웃만 사용
        val view = inflater.inflate(R.layout.item_playlist_result, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position])
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        private val ivCover: ImageView = view.findViewById(R.id.ivAlbumCover)

        fun bind(item: MusicModel) {
            tvTitle.text = item.title
            tvArtist.text = item.artist

            if (item.albumCover.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.albumCover)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .placeholder(R.color.black)
                    .error(R.color.black)
                    .into(ivCover)
            } else {
                ivCover.setImageResource(R.color.black)
            }
        }
    }
}