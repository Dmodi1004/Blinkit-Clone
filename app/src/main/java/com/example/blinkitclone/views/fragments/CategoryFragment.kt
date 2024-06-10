package com.example.blinkitclone.views.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.CartListener
import com.example.blinkitclone.R
import com.example.blinkitclone.adapters.ProductAdapter
import com.example.blinkitclone.databinding.FragmentCategoryBinding
import com.example.blinkitclone.databinding.ItemViewProductBinding
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.roomDB.CartProducts
import com.example.blinkitclone.utils.showToast
import com.example.blinkitclone.viewModels.UserViewModel
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private val binding: FragmentCategoryBinding by lazy {
        FragmentCategoryBinding.inflate(layoutInflater)
    }

    private var category: String? = null
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var cartListener: CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setStatusBarColor()
        getProductCategory()
        setToolbarTitle()
        fetchCategoryProducts()
        onSearchMenuClick()
        onNavigationItemClick()

        return binding.root
    }

    private fun onNavigationItemClick() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchMenu -> {
//                    changeMainFunctions(requireActivity(), SearchFragment())
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun fetchCategoryProducts() {
        binding.shimmerView.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getCategoryProduct(category!!).collect {
                if (it.isEmpty()) {
                    binding.productsRv.visibility = View.GONE
                    binding.text.visibility = View.VISIBLE
                } else {
                    binding.productsRv.visibility = View.VISIBLE
                    binding.text.visibility = View.GONE
                }
                adapter =
                    ProductAdapter(::onAddBtnClick, ::onIncrementBtnClick, ::onDecrementBtnClick)
                binding.productsRv.adapter = adapter
                adapter.differ.submitList(it)
                binding.shimmerView.visibility = View.GONE
            }
        }
    }

    private fun setToolbarTitle() {
        binding.toolbar.title = category
    }

    private fun getProductCategory() {
        val bundle = arguments
        category = bundle?.getString("category")
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

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
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