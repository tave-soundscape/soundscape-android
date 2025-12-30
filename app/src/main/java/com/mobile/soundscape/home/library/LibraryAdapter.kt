package com.mobile.soundscape.home.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.databinding.ItemLibraryPlaylistBinding // 바인딩 이름 확인
import com.mobile.soundscape.result.MusicModel

class LibraryAdapter(
    private val playlist: List<LibraryPlaylistModel>,
    private val onItemClick: (LibraryPlaylistModel) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemLibraryPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(playlist[position])
    }

    override fun getItemCount(): Int = playlist.size

    inner class LibraryViewHolder(private val binding: ItemLibraryPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LibraryPlaylistModel) {
            binding.tvPlaylistTitle.text = item.title
            // binding.tvPlaylistInfo.text = "곡 ${item.songs.size}개 • ${item.date}"

            // 아이템 클릭 리스너
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}