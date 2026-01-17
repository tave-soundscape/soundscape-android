package com.mobile.soundscape.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.PlaceDetail
import com.mobile.soundscape.databinding.ItemExplorePlaylistBinding
import com.mobile.soundscape.home.library.LabelMapper
import com.mobile.soundscape.result.MusicModel

class ExploreAdapter(
    private var items: MutableList<PlaceDetail> = mutableListOf() // MutableList로 변경
) : RecyclerView.Adapter<ExploreAdapter.ViewHolder>() {

    fun getItemList(): MutableList<PlaceDetail> = items
    fun updateData(newItems: List<PlaceDetail>) {
        this.items = newItems.toMutableList() // 새 데이터로 교체
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

            // [이미지 로직] 목록 API(imageUrl) -> 상세 API(songs[0]) 순서로 체크
            val imageToLoad = when {
                !item.imageUrl.isNullOrEmpty() -> item.imageUrl // 대표 이미지
                safeSongs.isNotEmpty() -> safeSongs[0].imageUrl // 상세 데이터 올 경우 첫 곡
                else -> ""
            }

            tvPlaylistTitle.text = item.title

            Glide.with(ivPlaylistCover.context)
                .load(imageToLoad)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(ivPlaylistCover)

            // [미니 커버] 상세 데이터(safeSongs)가 들어왔을 때만 보여줌
            val miniCovers = listOf(ivMiniCover1, ivMiniCover2, ivMiniCover3)

            if (safeSongs.isNotEmpty()) {
                miniCovers.forEachIndexed { index, imageView ->
                    if (index < safeSongs.size) {
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView.context)
                            .load(safeSongs[index].imageUrl)
                            .circleCrop()
                            .into(imageView)
                    } else {
                        imageView.visibility = View.GONE
                    }
                }
            } else {
                // 아직 상세 데이터를 못 받았으면 동그라미들을 일단 가림
                miniCovers.forEach { it.visibility = View.GONE }
            }

            // 4. 상세 페이지로 이동
            tvGoPlaylist.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("playlistId", item.id.toString()) // ID를 넘겨줍니다.
                    putString("title", item.title)
                }
                it.findNavController().navigate(R.id.action_exploreFragment_to_exploreDetailFragment, bundle)
            }

            // 5. 스포티파이 바로 재생 버튼
            btnPlay.setOnClickListener {
                if (!item.playlistUrl.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.playlistUrl))
                        it.context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("ExploreAdapter", "Spotify link failed")
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
    class ViewHolder(val binding: ItemExplorePlaylistBinding) : RecyclerView.ViewHolder(binding.root)
}