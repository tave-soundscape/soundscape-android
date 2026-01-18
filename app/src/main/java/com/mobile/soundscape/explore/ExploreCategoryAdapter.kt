package com.mobile.soundscape.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.databinding.ItemExploreCategoryBinding

class ExploreCategoryAdapter(
    private var categories: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ExploreCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    // [수정] 두 번째 인자 selectedCategory를 추가하여 상태를 복구합니다.
    fun updateData(newCategories: List<String>, selectedCategory: String? = null) {
        this.categories = newCategories

        // 텍스트를 기반으로 리스트에서 해당 카테고리의 위치(index)를 찾습니다.
        this.selectedPosition = if (selectedCategory != null) {
            val index = newCategories.indexOf(selectedCategory)
            if (index != -1) index else 0
        } else {
            0
        }

        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: ItemExploreCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, position: Int) {
            binding.tvCategoryName.text = category
            // 선택된 상태에 따라 UI 반영 (XML의 selector가 작동함)
            binding.tvCategoryName.isSelected = (selectedPosition == position)

            binding.root.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition

                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)

                    onItemClick(category)
                }
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