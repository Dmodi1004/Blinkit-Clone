package com.example.blinkitclone.views.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.FragmentOtpBinding
import com.example.blinkitclone.models.Users
import com.example.blinkitclone.utils.changeMainFunctions
import com.example.blinkitclone.utils.getCurrentUserId
import com.example.blinkitclone.utils.hideDialog
import com.example.blinkitclone.utils.showDialog
import com.example.blinkitclone.utils.showToast
import com.example.blinkitclone.viewModels.AuthViewModel
import com.example.blinkitclone.views.activities.UsersActivity
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private val binding: FragmentOtpBinding by lazy {
        FragmentOtpBinding.inflate(layoutInflater)
    }

    private lateinit var userNumber: String
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        getUserNumber()
        customizingEnterOTP()
        sendOTP()
        onLoginBtnClick()

        binding.toolbar.setNavigationOnClickListener {
//            findNavController().navigate(R.id.action_otpFragment_to_signinFragment)
            changeMainFunctions(requireActivity(), SigninFragment())
        }

        return binding.root
    }

    private fun onLoginBtnClick() {
        binding.apply {
            loginBtn.setOnClickListener {
                showDialog(requireContext(), "Signing In...")

                val editText = arrayOf(otp1Edt, otp2Edt, otp3Edt, otp4Edt, otp5Edt, otp6Edt)
                val otp = editText.joinToString("") { it.text.toString() }

                if (otp.length < editText.size || otp.length > editText.size) {
                    showToast(requireContext(), "Please enter right OTP")
                    hideDialog()
                } else {
                    editText.forEach { it.text?.clear(); it.clearFocus() }
                    verifyOtp(otp)
                }
            }
        }
    }

    private fun verifyOtp(otp: String) {

        val user =
            Users(uid = null, userNumber = userNumber, userAddress = "")

        viewModel.signInWithPhoneAuthCredential(otp, userNumber, user)
        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect {
                if (it) {
                    hideDialog()
                    showToast(requireContext(), "Logged In...")
                    startActivity(Intent(requireActivity(), UsersActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    private fun sendOTP() {
        showDialog(requireContext(), "Sending OTP...")
        viewModel.apply {
            sendOTP(userNumber, requireActivity())
            lifecycleScope.launch {
                otpSent.collect {
                    if (it) {
                        hideDialog()
                        showToast(requireContext(), "OTP sent successfully")
                    }
                }
            }
        }
    }

    private fun customizingEnterOTP() {
        binding.apply {

            val editText = arrayOf(otp1Edt, otp2Edt, otp3Edt, otp4Edt, otp5Edt, otp6Edt)

            for (i in editText.indices) {
                editText[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s?.length == 1) {
                            if (i < editText.size - 1) {
                                editText[i + 1].requestFocus()
                            }
                        } else if (s?.length == 0) {
                            if (i > 0) {
                                editText[i - 1].requestFocus()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()
        binding.userNumberTv.text = userNumber
    }

}