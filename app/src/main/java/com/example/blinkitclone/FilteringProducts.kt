package com.example.blinkitclone

import android.widget.Filter
import com.example.blinkitclone.adapters.ProductAdapter
import com.example.blinkitclone.models.Product
import java.util.Locale

class FilteringProducts(
    private val adapter: ProductAdapter,
    private val filter: ArrayList<Product>
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")
            val filteredList = ArrayList<Product>()

            for (products in filter) {
                if (query.any {
                        products.productTitle?.uppercase(Locale.getDefault())
                            ?.contains(it) == true ||
                                products.productCategory?.uppercase(Locale.getDefault())
                                    ?.contains(it) == true ||
                                products.productPrice?.toString()?.uppercase(Locale.getDefault())
                                    ?.contains(it) == true ||
                                products.productType?.uppercase(Locale.getDefault())
                                    ?.contains(it) == true
                    }) {
                    filteredList.add(products)
                }
            }
            result.values = filteredList
            result.count = filteredList.size
        } else {
            result.values = filter
            result.count = filter.size
        }
        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<Product>)
    }
}