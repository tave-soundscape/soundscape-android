package com.mobile.soundscape.result

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

class PlaylistResultAdapter(
    private val songList: List<MusicModel>,
    private val onFooterClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SONG = 0
    private val TYPE_FOOTER = 1

    override fun getItemCount(): Int = songList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == songList.size) TYPE_FOOTER else TYPE_SONG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SONG) {
            // item_listmode.xml 레이아웃 사용
            val view = inflater.inflate(R.layout.item_playlist_result, parent, false)
            SongViewHolder(view)
        } else {
            // item_playlist_footer.xml 레이아웃 사용
            val view = inflater.inflate(R.layout.item_playlist_result_footer, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SongViewHolder) {
            holder.bind(songList[position])
        } else if (holder is FooterViewHolder) {
            holder.itemView.setOnClickListener {
                holder.footerText.setOnClickListener {
                    onFooterClick()
                }
            }
        }
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle) // 노래 제목 ID
        private val tvArtist: TextView = view.findViewById(R.id.tvArtist) // [추가됨] 가수 이름 ID
        private val ivCover: ImageView = view.findViewById(R.id.ivAlbumCover) // 앨범 커버 ID

        fun bind(item: MusicModel) {
            tvTitle.text = item.title
            tvArtist.text = item.artist // 가수 이름 설정

            // Glide로 이미지 로드
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

    inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val footerText: TextView = view.findViewById(R.id.tvFooterText)
    }
}