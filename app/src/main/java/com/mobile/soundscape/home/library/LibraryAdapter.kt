package com.mobile.soundscape.home.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ItemLibraryLikedBinding
import com.mobile.soundscape.databinding.ItemLibraryPlaylistBinding
import com.mobile.soundscape.databinding.ItemLibrarySpotifyCoverBinding

class LibraryAdapter(
    private val items: List<LibraryPlaylistModel>,
    private val onItemClick: (LibraryPlaylistModel?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_LIKED = 0    // 맨 앞 '좋아요' 카드
        const val TYPE_PLAYLIST = 1 // 일반 플레이리스트 카드
    }

    // 아이템 개수는 (실제 데이터 개수 + 좋아요 카드 1개)
    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_LIKED else TYPE_PLAYLIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_LIKED) {
            val binding = ItemLibraryLikedBinding.inflate(inflater, parent, false)
            LikedViewHolder(binding)
        } else {
            val binding = ItemLibrarySpotifyCoverBinding.inflate(inflater, parent, false)
            PlaylistViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LikedViewHolder) {
            holder.bind()
        } else if (holder is PlaylistViewHolder) {
            // position 0은 좋아요 카드이므로, 실제 데이터는 index - 1 에서 가져옴
            val item = items[position - 1]
            holder.bind(item)
        }
    }

    // [1] 좋아요 카드 뷰홀더
    inner class LikedViewHolder(private val binding: ItemLibraryLikedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            itemView.setOnClickListener {
                onItemClick(null) // null을 넘겨서 좋아요 클릭임을 알림
            }
        }
    }

    // [2] 일반 플레이리스트 뷰홀더 (2x2 이미지 처리)
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
                binding.ivCoverMain.setImageResource(R.drawable.ic_launcher_background)
            }


            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}