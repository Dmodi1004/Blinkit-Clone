package com.example.blinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blinkitclone.databinding.ItemViewCartProductsBinding
import com.example.blinkitclone.roomDB.CartProducts

class CartProductsAdapter : RecyclerView.Adapter<CartProductsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemViewCartProductsBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    val diffUtil = object : DiffUtil.ItemCallback<CartProducts>() {
        override fun areItemsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemViewCartProductsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val products = differ.currentList[position]

        holder.binding.apply {
            Glide.with(holder.itemView)
                .load(products.productImage)
                .into(productImageIv)

            productTitleTv.text = products.productTitle
            productQuantityTv.text = products.productQuantity
            productPriceTv.text = products.productPrice
            productCountTv.text = products.productCount.toString()
        }
    }
}