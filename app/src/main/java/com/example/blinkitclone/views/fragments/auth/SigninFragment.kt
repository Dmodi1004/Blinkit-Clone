package com.example.blinkitclone.views.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.FragmentSigninBinding
import com.example.blinkitclone.utils.changeMainFunctions
import com.example.blinkitclone.utils.showToast
import com.example.blinkitclone.viewModels.AuthViewModel
import com.example.blinkitclone.views.activities.UsersActivity
import kotlinx.coroutines.launch

class SigninFragment : Fragment() {

    private val binding: FragmentSigninBinding by lazy {
        FragmentSigninBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        getUserNumber()
        onContinueBtnClick()

        return binding.root
    }

    private fun onContinueBtnClick() {
        binding.continueBtn.setOnClickListener {
            val number = binding.userNumberEdt.text.toString().trim()

            if (number.isEmpty() || number.length != 10) {
                showToast(requireContext(), "Please enter valid number")
            } else {
                val bundle = Bundle()
                bundle.putString("number", number)
//                findNavController().navigate(R.id.action_signinFragment_to_otpFragment, bundle)
                val otpFragment = OtpFragment()
                otpFragment.arguments = bundle
                changeMainFunctions(requireActivity(), otpFragment)
            }
        }
    }

    private fun getUserNumber() {
        binding.userNumberEdt.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    number: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val length = number?.length

                    if (length == 10) {
                        binding.continueBtn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.green
                            )
                        )
                        binding.continueBtn.isEnabled = true
                        binding.continueBtn.isClickable = true
                    } else {
                        binding.continueBtn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.grayishBlue
                            )
                        )
                        binding.continueBtn.isEnabled = false
                        binding.continueBtn.isClickable = false
                    }

                }

                override fun afterTextChanged(s: Editable?) {}
            })
    }

}