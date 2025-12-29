package com.mobile.soundscape.home.library

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

// 푸터 없이 곡 목록만 깔끔하게 보여주는 전용 어댑터입니다.
class LibraryDetailAdapter(
    private val songList: List<MusicModel>
) : RecyclerView.Adapter<LibraryDetailAdapter.SongViewHolder>() {

    override fun getItemCount(): Int = songList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // [중요] 배경이 투명한 사용자님의 레이아웃을 사용합니다.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_detail_song, parent, false)
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

            // 동료분의 Glide 로직을 그대로 사용합니다.
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