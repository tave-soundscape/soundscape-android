package com.mobile.soundscape.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.mobile.soundscape.R

class ArtistAdapter(
    var artistList: List<ArtistData>, // 외부에서 접근 가능하도록 var로 변경
    private val onArtistClick: (ArtistData, Int) -> Unit // 클릭 시 데이터와 위치를 보냄
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_artist_image)
        val imageView: ImageView = itemView.findViewById(R.id.iv_artist)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_artist_name)
        val checkIcon: ImageView = itemView.findViewById(R.id.isSelected_check)

        fun bind(artist: ArtistData) {
            nameTextView.text = artist.name

            // Glide로 이미지 로드
            if (artist.imageResId.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(artist.imageResId) // URL 로드
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16))) // 둥근 모서리 옵션 (선택)
                    .placeholder(R.color.gray700) // 로딩 중 배경색
                    .error(R.color.gray700)       // 에러 시 배경색
                    .into(imageView)
            } else {
                // 이미지가 없을 때 기본 처리
                imageView.setImageResource(R.color.gray700)
            }

            // 선택 상태에 따른 테두리 처리
            if (artist.isSelected) {
                cardView.alpha = 0.5f // 선택된 느낌을 주기 위해 살짝 투명하게
                cardView.strokeWidth = 12
                cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.white) // 선택 색상
                checkIcon.visibility = View.VISIBLE
            } else {
                cardView.strokeWidth = 0
                cardView.alpha = 1.0f
                checkIcon.visibility = View.GONE
            }

            // 클릭 이벤트 -> 프래그먼트로 토스!
            itemView.setOnClickListener {
                onArtistClick(artist, bindingAdapterPosition)
            }
        }
    }

    fun updateList(newList: List<ArtistData>) {
        artistList = newList
        notifyDataSetChanged()
    }

    // 선택 초기화 (UI 갱신용)
    fun clearSelection() {
        artistList.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        // item_artist_selection.xml 레이아웃 사용
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist_selection, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artistList[position])
    }

    override fun getItemCount(): Int = artistList.size
}