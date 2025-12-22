package com.mobile.soundscape.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.databinding.ItemExplorePlaylistBinding

class ExplorePlaylistAdapter(
    private var playlists: List<ExplorePlaylist>
) : RecyclerView.Adapter<ExplorePlaylistAdapter.PlaylistViewHolder>() {

    fun updateData(newPlaylists: List<ExplorePlaylist>) {
        this.playlists = newPlaylists // 새로운 리스트로 교체
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(private val binding: ItemExplorePlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExplorePlaylist) {
            binding.tvPlaylistTitle.text = item.title
            binding.tvPlaylistDescription.text = item.description
            binding.tvPlaylistTag.text = item.tag
            // 임시 이미지(drawable) 설정
            binding.ivPlaylistCover.setImageResource(item.coverImage)


            binding.btnAddLibrary.setOnClickListener {
                item.isSaved = true
                Toast.makeText(
                    binding.root.context,
                    "라이브러리에 추가되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemExplorePlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])

    }

    override fun getItemCount(): Int = playlists.size
}