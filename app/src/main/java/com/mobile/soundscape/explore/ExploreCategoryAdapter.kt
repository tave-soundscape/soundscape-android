package com.mobile.soundscape.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.databinding.ItemExploreCategoryBinding

class ExploreCategoryAdapter(
    private var categories: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ExploreCategoryAdapter.CategoryViewHolder>() {

    // 현재 선택된 아이템의 위치 (기본값: 0번째 '전체' 또는 '집/실내')
    private var selectedPosition = 0

    fun updateData(newCategories: List<String>) {
        this.categories = newCategories
        this.selectedPosition = 0 // 카테고리 종류가 바뀌면 선택 위치를 처음으로 초기화
        notifyDataSetChanged() // 리스트 전체를 다시 그림
    }
    inner class CategoryViewHolder(private val binding: ItemExploreCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, position: Int) {
            binding.tvCategoryName.text = category

            // 선택된 상태에 따라 배경 변경
            binding.tvCategoryName.isSelected = (selectedPosition == position)

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // 이전 선택 항목과 현재 선택 항목만 새로고침
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onItemClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemExploreCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size
}