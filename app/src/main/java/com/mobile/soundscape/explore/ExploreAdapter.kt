package com.mobile.soundscape.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.PlaceDetail
import com.mobile.soundscape.databinding.ItemExplorePlaylistBinding

class ExploreAdapter(
    private var items: MutableList<PlaceDetail> = mutableListOf(),
    private val onAddClick: (PlaceDetail) -> Unit,
    private val onPlayClick: (PlaceDetail) -> Unit,
    private val onDetailClick: (PlaceDetail) -> Unit
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

            // --- [아티스트명 및 곡 개수 동적 조합 로직] ---
            val songCount = safeSongs.size // 곡 개수

            val artists = safeSongs
                .mapNotNull { it.artistName }
                .filter { it.isNotBlank() }
                .distinct()
                .take(4)

            val artistDisplayText = if (artists.isNotEmpty()) {
                artists.joinToString(", ") + " 등"
            } else {
                "다양한 아티스트"
            }

            // XML의 ID인 tvPlaylistDescription에 결합해서 세팅
            tvPlaylistDescription.text = "곡 ${songCount}개 • $artistDisplayText"
            // ------------------------------------------

            // 2. 이미지 로직
            val imageToLoad = when {
                !item.imageUrl.isNullOrEmpty() -> item.imageUrl
                safeSongs.isNotEmpty() -> safeSongs[0].imageUrl
                else -> ""
            }
            tvPlaylistTitle.text = item.title

            Glide.with(ivPlaylistCover.context)
                .load(imageToLoad)
                .placeholder(R.drawable.img_placeholder)
                .into(ivPlaylistCover)

            // 3. 미니 커버 로직
            val miniCovers = listOf(ivMiniCover1, ivMiniCover2, ivMiniCover3)
            if (safeSongs.size >= 2) {
                miniCovers.forEachIndexed { index, imageView ->
                    val songIndex = index + 1
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

            // 4. 클릭 리스너 연결
            btnAddLibrary.setOnClickListener { onAddClick(item) }
            btnPlay.setOnClickListener { onPlayClick(item) }
            tvGoPlaylist.setOnClickListener { onDetailClick(item) } // 여기서 item을 넘겨줍니다.
        }
    }

    override fun getItemCount(): Int = items.size
    class ViewHolder(val binding: ItemExplorePlaylistBinding) : RecyclerView.ViewHolder(binding.root)
}