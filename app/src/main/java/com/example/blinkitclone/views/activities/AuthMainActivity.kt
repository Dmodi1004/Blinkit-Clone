package com.example.blinkitclone.views.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.R
import com.example.blinkitclone.utils.changeMainFunctions
import com.example.blinkitclone.viewModels.AuthViewModel
import com.example.blinkitclone.views.fragments.auth.SigninFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthMainActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(2000)
        installSplashScreen()
        setContentView(R.layout.activity_auth_main)

        changeMainFunctions(this@AuthMainActivity, SigninFragment())

        lifecycleScope.launch {
            viewModel.isCurrentUser.collect{
                if(it){
                    startActivity(Intent(this@AuthMainActivity, UsersActivity::class.java))
                    finish()
                } else{
                    changeMainFunctions(this@AuthMainActivity, SigninFragment())
                }
            }
        }

    }

}