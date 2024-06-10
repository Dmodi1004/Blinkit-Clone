package com.example.blinkitclone.views.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.adapters.CategoryAdapter
import com.example.blinkitclone.databinding.FragmentHomeBinding
import com.example.blinkitclone.models.Category
import com.example.blinkitclone.utils.Constants
import com.example.blinkitclone.utils.changeMainFunctions
import com.example.blinkitclone.viewModels.UserViewModel

class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setStatusBarColor()
        setAllCategories()
        get()

        binding.searchTv.setOnClickListener {
//            changeMainFunctions(requireActivity(), SearchFragment())
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        return binding.root
    }

    private fun get() {
        viewModel.getAll().observe(viewLifecycleOwner){
            for (i in it){
                Log.d("TAG", "room: ${i.productTitle}")
                Log.d("TAG", "room: ${i.productCount}")
            }
        }
    }

    fun onCategoryItemClick(category: Category) {
        val bundle = Bundle()
        bundle.putString("category", category.title)
        /*val categoryFragment = CategoryFragment()
        categoryFragment.arguments = bundle
        changeMainFunctions(requireActivity(), categoryFragment)*/
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()

        for (i in 0 until Constants.allProductsCategoryIcon.size) {
            categoryList.add(
                Category(
                    Constants.allProductCategory[i],
                    Constants.allProductsCategoryIcon[i]
                )
            )
        }
        binding.categoriesRv.adapter = CategoryAdapter(categoryList, ::onCategoryItemClick)
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.orange)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}