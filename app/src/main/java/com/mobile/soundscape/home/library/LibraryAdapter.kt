package com.mobile.soundscape.home.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ItemLibrarySpotifyCoverBinding

class LibraryAdapter(
    private val items: List<LibraryPlaylistModel>,
    private val onItemClick: (LibraryPlaylistModel) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.PlaylistViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemLibrarySpotifyCoverBinding.inflate(inflater, parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    // 일반 플레이리스트 뷰홀더 (2x2 이미지 처리)
    inner class PlaylistViewHolder(private val binding: ItemLibrarySpotifyCoverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibraryPlaylistModel) {
            // 제목 설정
            binding.tvPlaylistTitle.text = item.title

            // 장소/목표 or 곡 개수 표시
            val rawLocation = item.location ?: ""
            val rawGoal = item.goal ?: ""

            if (rawLocation == "old_playlist" || rawGoal == "old_playlist") {
             binding.tvSubtitle.text = "곡 ${item.songCount}개"
            } else {
                val kPlace = LabelMapper.getKoreanPlace(rawLocation)
                val kGoal = LabelMapper.getKoreanGoal(rawGoal)

                binding.tvSubtitle.text = "$kPlace • $kGoal"
            }

            // 커버 이미지 설정 (spotify에서 4분할 이미지 받아온 거)
            if(!item.mainCoverUrl.isNullOrEmpty()){
                Glide.with(itemView.context)
                    .load(item.mainCoverUrl)
                    .placeholder(R.color.gray800)
                    .error(R.color.gray600)       // 에러 시 보여줄 색
                    .into(binding.ivCoverMain)
            } else {
                // 이미지가 없을 때 기본 이미지 처리
                binding.ivCoverMain.setImageResource(R.drawable.bg_input_rounded)
            }


            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}