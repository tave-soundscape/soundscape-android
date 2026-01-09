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
            val binding = ItemLibraryPlaylistBinding.inflate(inflater, parent, false)
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
    inner class PlaylistViewHolder(private val binding: ItemLibraryPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibraryPlaylistModel) {
            binding.tvPlaylistTitle.text = item.title
            binding.tvSubtitle.text = "곡 ${item.songCount}개"

            // 1. MusicModel 리스트에서 앨범 커버 URL만 최대 4개 추출
            val coverUrls = item.songs.take(4).map { it.albumCover }

            val imageViews = listOf(
                binding.ivCover1,
                binding.ivCover2,
                binding.ivCover3,
                binding.ivCover4
            )

            // 2. 초기화 (재사용 문제 방지)
            imageViews.forEach {
                it.setImageDrawable(null)
                it.setBackgroundColor(0xFF333333.toInt()) // 빈 공간은 진한 회색

                // 레이아웃 초기화 (2x2 모드로 복구)
                val params = it.layoutParams as ConstraintLayout.LayoutParams
                params.matchConstraintPercentWidth = 0.5f
                params.matchConstraintPercentHeight = 0.5f
                it.layoutParams = params
                it.visibility = View.VISIBLE
            }
            binding.ivEmptyPlaceholder.visibility = View.GONE


            // 3. 이미지 개수에 따른 로직 분기
            if (coverUrls.isEmpty()) {
                // 곡이 없을 때 -> 음표 아이콘 표시
                binding.ivEmptyPlaceholder.visibility = View.VISIBLE

            } else if (coverUrls.size < 4) {
                // 곡이 1~3개일 때 -> 첫 번째 곡 커버를 크게 꽉 채워서 보여줌
                val params = binding.ivCover1.layoutParams as ConstraintLayout.LayoutParams
                params.matchConstraintPercentWidth = 1f
                params.matchConstraintPercentHeight = 1f
                binding.ivCover1.layoutParams = params

                Glide.with(itemView.context).load(coverUrls[0]).into(binding.ivCover1)

                // 나머지 뷰는 가리기 (1번이 덮어버림)

            } else {
                // 곡이 4개 이상일 때 -> 2x2 격자로 4개 모두 로드
                for (i in 0 until 4) {
                    Glide.with(itemView.context)
                        .load(coverUrls[i])
                        .into(imageViews[i])
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}