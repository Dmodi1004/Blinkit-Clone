package com.example.blinkitclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitclone.FilteringProducts
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Product

class ProductAdapter(
    val onAddBtnClick: (Product, ItemViewProductBinding) -> Unit,
    val onIncrementBtnClick: (Product, ItemViewProductBinding) -> Unit,
    val onDecrementBtnClick: (Product, ItemViewProductBinding) -> Unit
) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable {

    class ViewHolder(val binding: ItemViewProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemViewProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()
            val productImage = product.productImageUri

            for (i in 0 until productImage?.size!!) {
                imageList.add(SlideModel(product.productImageUri!![i].toString()))
            }

            imageSlider.setImageList(imageList)
            productTitleTv.text = product.productTitle
            val quantity = product.productQuantity.toString() + product.productUnit
            productQuantityTv.text = quantity
            productPriceTv.text = "₹${product.productPrice}"

            if (product.itemCount!! > 0) {
                productCountTv.text = product.itemCount.toString()
                addTv.visibility = View.GONE
                productCountLl.visibility = View.VISIBLE
            }
            addTv.setOnClickListener {
                onAddBtnClick(product, this)
            }
            incrementCountTv.setOnClickListener {
                onIncrementBtnClick(product, this)
            }
            decrementCountTv.setOnClickListener {
                onDecrementBtnClick(product, this)
            }
        }
        /*holder.itemView.setOnClickListener {
            onEditBtnClick(product)
        }*/
    }

    val filter: FilteringProducts? = null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if (filter == null) return FilteringProducts(this, originalList)
        return filter
    }


}
