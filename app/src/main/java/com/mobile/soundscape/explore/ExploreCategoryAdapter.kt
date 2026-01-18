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
    fun updateData(newCategories: List<String>, selectedCategory: String? = null) {
        this.categories = newCategories

        // 넘겨받은 텍스트가 리스트의 몇 번째인지 찾아 selectedPosition을 갱신합니다.
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
            binding.tvCategoryName.isSelected = (selectedPosition == position)

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
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