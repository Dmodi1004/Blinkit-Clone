package com.example.blinkitclone.viewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.blinkitclone.models.Users
import com.example.blinkitclone.utils.getAuthInstance
import com.example.blinkitclone.utils.getCurrentUserId
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)

    private val _otpSent = MutableStateFlow(false)
    val otpSent = _otpSent

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser = _isCurrentUser

    init {
        getAuthInstance().currentUser?.let {
            _isCurrentUser.value = true
        }
    }

    fun sendOTP(userNumber: String, activity: Activity) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: FirebaseException) {}

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true
            }
        }

        val options = PhoneAuthOptions.newBuilder(getAuthInstance())
            .setPhoneNumber("+91$userNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {

        val credential = PhoneAuthProvider.getCredential(_verificationId.value.toString(), otp)

        getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.uid = getCurrentUserId()
                    FirebaseDatabase.getInstance().getReference().child("All Users").child("Users")
                        .child(user.uid!!).setValue(user)
                    _isSignedInSuccessfully.value = true
                } else {

                }
            }
    }

}