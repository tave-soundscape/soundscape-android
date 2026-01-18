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
                miniCovers.forEach { it.visibility = View.GONE }
            }

            btnAddLibrary.setOnClickListener { onAddClick(item) }
            tvGoPlaylist.setOnClickListener { onDetailClick(item) }
            btnPlay.setOnClickListener { onPlayClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size
    class ViewHolder(val binding: ItemExplorePlaylistBinding) : RecyclerView.ViewHolder(binding.root)
}