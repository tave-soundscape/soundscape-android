package com.mobile.soundscape.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.R

class GenreAdapter(
    var genreList: List<GenreData>, // 외부에서 접근 가능하도록 var로 변경
    private val onGenreClick: (GenreData, Int) -> Unit // 클릭 시 데이터와 위치를 보냄
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {
    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // XML에 정의된 뷰들과 연결
        private val backgroundImageView: ImageView = itemView.findViewById(R.id.iv_genre_card)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_genre_name)

        fun bind(genre: GenreData) {
            // 장르 이름 설정
            nameTextView.text = genre.name

            // 선택 상태에 따른 UI 처리
            if (genre.isSelected) {
                // 선택되었을 때: 배경을 어둡게(투명하게) 하고 체크 아이콘 표시
                backgroundImageView.alpha = 0.5f
            } else {
                // 선택 안 되었을 때: 배경 원상복구, 체크 아이콘 숨김
                backgroundImageView.alpha = 1.0f
            }

            // 클릭 이벤트 처리
            itemView.setOnClickListener {
                onGenreClick(genre, bindingAdapterPosition)
            }
        }
    }

    // 레이아웃 생성 (item_genre_selection.xml 이라고 가정)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre_selection, parent, false)
        return GenreViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(genreList[position])
    }

    // 데이터 개수 반환
    override fun getItemCount(): Int = genreList.size

    // 리스트 업데이트 함수
    fun updateList(newList: List<GenreData>) {
        genreList = newList
        notifyDataSetChanged()
    }

    // 선택 초기화
    fun clearSelection() {
        genreList.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }
}