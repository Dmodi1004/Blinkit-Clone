package com.example.blinkitclone.views.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.CartListener
import com.example.blinkitclone.R
import com.example.blinkitclone.adapters.ProductAdapter
import com.example.blinkitclone.databinding.FragmentSearchBinding
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.roomDB.CartProducts
import com.example.blinkitclone.utils.showToast
import com.example.blinkitclone.viewModels.UserViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private val binding: FragmentSearchBinding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: ProductAdapter
    private val viewModel: UserViewModel by viewModels()
    private var cartListener: CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        getAllProducts()
        searchProducts()

        binding.backBtn.setOnClickListener {
//            changeMainFunctions(requireActivity(), HomeFragment())
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        return binding.root
    }

    private fun getAllProducts() {
        binding.shimmerView.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.fetchProducts().collect {

                if (it.isEmpty()) {
                    binding.productsRv.visibility = View.GONE
                    binding.text.visibility = View.VISIBLE
                } else {
                    binding.productsRv.visibility = View.VISIBLE
                    binding.text.visibility = View.GONE
                }
                adapter = ProductAdapter(
                    ::onAddBtnClick,
                    ::onIncrementBtnClick,
                    ::onDecrementBtnClick
                )
                binding.productsRv.adapter = adapter
                adapter.differ.submitList(it)
                adapter.originalList = it as ArrayList<Product>
                binding.shimmerView.visibility = View.GONE
            }
        }

    }

    private fun searchProducts() {
        binding.searchEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapter.getFilter().filter(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onAddBtnClick(product: Product, productBinding: ItemViewProductBinding) {
        productBinding.addTv.visibility = View.GONE
        productBinding.productCountLl.visibility = View.VISIBLE

        var itemCount = productBinding.productCountTv.text.toString().toInt()
        itemCount++
        productBinding.productCountTv.text = itemCount.toString()

        cartListener?.showCartLayout(1)

        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductsInRoom(product)
            viewModel.updateItemCount(product, itemCount)
        }
    }

    private fun onIncrementBtnClick(product: Product, productBinding: ItemViewProductBinding) {
        var itemCountInc = productBinding.productCountTv.text.toString().toInt()
        itemCountInc++

        if (product.productStock!! + 1 > itemCountInc) {
            productBinding.productCountTv.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)
            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductsInRoom(product)
                viewModel.updateItemCount(product, itemCountInc)
            }
        } else{
            showToast(requireContext(), "Can't add more item of this")
        }
    }

    private fun onDecrementBtnClick(product: Product, productBinding: ItemViewProductBinding) {
        var itemCountDec = productBinding.productCountTv.text.toString().toInt()
        itemCountDec--

        cartListener?.showCartLayout(-1)
        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductsInRoom(product)
            viewModel.updateItemCount(product, itemCountDec)
        }

        if (itemCountDec > 0)
            productBinding.productCountTv.text = itemCountDec.toString()
        else {
            lifecycleScope.launch {
                viewModel.deleteCartProduct(product.productRandomId!!)
            }
            productBinding.addTv.visibility = View.VISIBLE
            productBinding.productCountLl.visibility = View.GONE
            productBinding.productCountTv.text = "0"
        }

    }

    private fun saveProductsInRoom(product: Product) {
        val cartProducts = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "₹${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUri?.get(0),
            productCategory = product.productCategory,
            adminUid = product.adminUid
        )

        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProducts)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CartListener) {
            cartListener = context
        } else {
            throw ClassCastException("Please implement cart listener")
        }
    }

}