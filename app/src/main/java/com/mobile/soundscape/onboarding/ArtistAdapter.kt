package com.mobile.soundscape.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mobile.soundscape.onboarding.ArtistData
import com.mobile.soundscape.R

class ArtistAdapter(
    private var artistList: List<ArtistData>,
    private val onItemClick: () -> Unit // Int를 넘기지 않고, 클릭됐다는 신호만 줌
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_artist_image)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_artist_name)

        fun bind(artist: ArtistData) {
            nameTextView.text = artist.name

            // 테두리 그리기 (동일)
            if (artist.isSelected) {
                cardView.strokeWidth = 12
                cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.white)
            } else {
                cardView.strokeWidth = 0
            }

            itemView.setOnClickListener {
                // 1. 현재 전체 리스트에서 선택된 개수 세기
                // (artistList가 전체 목록을 가지고 있으므로 여기서 세면 됩니다)
                val currentSelectedCount = artistList.count { it.isSelected }

                // 2. [핵심] 제한 로직 추가
                // "이미 3개 이상 선택됨" AND "지금 누른 건 선택 안 된 놈임" -> 그러면 막아라!
                if (currentSelectedCount >= 3 && !artist.isSelected) {
                    return@setOnClickListener // 여기서 함수 종료! (밑에 코드 실행 안 됨)
                }

                // 3. 상태 변경 (제한에 안 걸렸을 때만 실행됨)
                artist.isSelected = !artist.isSelected
                notifyItemChanged(bindingAdapterPosition)

                onItemClick()
            }
        }
    }

    fun updateList(newList: List<ArtistData>) {
        artistList = newList
        notifyDataSetChanged()
    }

    fun clearSelection() {
        artistList.forEach { it.isSelected = false }
        notifyDataSetChanged()
        onItemClick() // 초기화됐으니 다시 계산하라고 알림
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist_selection, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artistList[position])
    }

    override fun getItemCount(): Int = artistList.size
}