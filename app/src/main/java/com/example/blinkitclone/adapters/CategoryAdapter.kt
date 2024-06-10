package com.example.blinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkitclone.databinding.ItemListProductCategoryBinding
import com.example.blinkitclone.models.Category

class CategoryAdapter(
    private val categoryList: ArrayList<Category>,
    val onCategoryItemClick: (Category) -> Unit
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListProductCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListProductCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category = categoryList[position]
        holder.binding.apply {
            categoryImg.setImageResource(category.image)
            categoryTitleTv.text = category.title
        }
        holder.itemView.setOnClickListener {
            onCategoryItemClick(category)
        }
    }
}