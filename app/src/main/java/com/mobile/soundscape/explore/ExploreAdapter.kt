package com.mobile.soundscape.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.PlaceDetail
import com.mobile.soundscape.databinding.ItemExplorePlaylistBinding

class ExploreAdapter(
    private var items: MutableList<PlaceDetail> = mutableListOf(),
    private val onAddClick: (PlaceDetail) -> Unit,
    private val onPlayClick: (PlaceDetail) -> Unit // 1. -> Unit 추가 (컴파일 에러 해결)
) : RecyclerView.Adapter<ExploreAdapter.ViewHolder>() {

    fun getItemList(): MutableList<PlaceDetail> = items
    fun updateData(newItems: List<PlaceDetail>) {
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExplorePlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            val safeSongs = item.songs ?: emptyList()

            // [이미지 로직]
            val imageToLoad = when {
                !item.imageUrl.isNullOrEmpty() -> item.imageUrl
                safeSongs.isNotEmpty() -> safeSongs[0].imageUrl
                else -> ""
            }

            tvPlaylistTitle.text = item.title

            Glide.with(ivPlaylistCover.context)
                .load(imageToLoad)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(ivPlaylistCover)

            // [수정된 미니 커버 로직] 2, 3, 4번 곡을 원형으로 보여줌
            val miniCovers = listOf(ivMiniCover1, ivMiniCover2, ivMiniCover3)
            if (safeSongs.size >= 2) {
                miniCovers.forEachIndexed { index, imageView ->
                    val songIndex = index + 1 // 곡 2, 3, 4 순서
                    if (songIndex < safeSongs.size) {
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView.context)
                            .load(safeSongs[songIndex].imageUrl)
                            .circleCrop()
                            .into(imageView)
                    } else {
                        imageView.visibility = View.GONE
                    }
                }
            } else {
                miniCovers.forEach { it.visibility = View.GONE }
            }

            // [추가] 4. 라이브러리 추가 (+) 버튼 연결
            btnAddLibrary.setOnClickListener {
                onAddClick(item)
            }

            // 5. 상세 페이지로 이동
            tvGoPlaylist.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("playlistId", item.id.toString())
                    putString("title", item.title)
                }
                it.findNavController().navigate(R.id.action_exploreFragment_to_exploreDetailFragment, bundle)
            }

            // 6. 스포티파이 재생 버튼 (바텀시트 호출)
            btnPlay.setOnClickListener {
                onPlayClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
    class ViewHolder(val binding: ItemExplorePlaylistBinding) : RecyclerView.ViewHolder(binding.root)
}