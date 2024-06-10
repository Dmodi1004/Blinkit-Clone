package com.example.blinkitclone.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.blinkitclone.CartListener
import com.example.blinkitclone.adapters.CartProductsAdapter
import com.example.blinkitclone.databinding.ActivityUsersBinding
import com.example.blinkitclone.databinding.BottomsheetCartProductsBinding
import com.example.blinkitclone.roomDB.CartProducts
import com.example.blinkitclone.viewModels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersActivity : AppCompatActivity(), CartListener {

    private val binding: ActivityUsersBinding by lazy {
        ActivityUsersBinding.inflate(layoutInflater)
    }

    private val viewModel: UserViewModel by viewModels()
    private lateinit var cartProductList: List<CartProducts>
    private lateinit var cartProductsAdapter: CartProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getTotalItemCount()
        onCartClick()
        getAllCartProducts()

        binding.nextBtn.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }

    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) {
            cartProductList = it
        }
    }

    private fun onCartClick() {
        binding.cartItemLl.setOnClickListener {
            val bottomsheetCartProductsBinding = BottomsheetCartProductsBinding.inflate(
                LayoutInflater.from(this)
            )
            val bs = BottomSheetDialog(this)
            bs.setContentView(bottomsheetCartProductsBinding.root)

            bottomsheetCartProductsBinding.numberOfProductCountTv.text =
                binding.numberOfProductCountTv.text
            bottomsheetCartProductsBinding.nextBtn.setOnClickListener {
                startActivity(Intent(this, OrderPlaceActivity::class.java))
            }
            cartProductsAdapter = CartProductsAdapter()
            bottomsheetCartProductsBinding.productsItemRv.adapter = cartProductsAdapter
            cartProductsAdapter.differ.submitList(cartProductList)

            bs.show()
        }
    }

    private fun getTotalItemCount() {
        viewModel.fetchTotalItemCount().observe(this) {
            if (it > 0) {
                binding.cartLl.visibility = View.VISIBLE
                binding.numberOfProductCountTv.text = it.toString()
            } else {
                binding.cartLl.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.numberOfProductCountTv.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if (updatedCount > 0) {
            binding.cartLl.visibility = View.VISIBLE
            binding.numberOfProductCountTv.text = updatedCount.toString()
        } else {
            binding.cartLl.visibility = View.GONE
            binding.numberOfProductCountTv.text = "0"
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalItemCount().observe(this) {
            viewModel.savingCartItemCount(it + itemCount)
        }
    }


}