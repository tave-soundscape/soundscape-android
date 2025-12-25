package com.mobile.soundscape.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.result.MusicModel
import com.mobile.soundscape.R

class GalleryAdapter(private val musicList: List<MusicModel>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cover: ImageView = itemView.findViewById(R.id.ivAlbumCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_cover, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        // [수정] 무한 스크롤을 위해 나머지 연산(%) 사용
        // 예: position이 100이고 리스트가 5개면 -> 100 % 5 = 0번째 데이터 사용
        val realPosition = position % musicList.size
        val music = musicList[realPosition]

        Glide.with(holder.itemView.context)
            .load(music.albumCover)
            .into(holder.cover)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE
}