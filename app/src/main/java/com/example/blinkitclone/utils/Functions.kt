package com.example.blinkitclone.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.blinkitclone.R
import com.example.blinkitclone.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private var dialog: AlertDialog? = null
fun showDialog(context: Context, message: String) {
    val progress = ProgressDialogBinding.inflate(LayoutInflater.from(context))
    progress.messageTv.text = message
    dialog = AlertDialog.Builder(context)
        .setView(progress.root)
        .setCancelable(false)
        .create()
    dialog!!.show()
}

fun hideDialog() {
    dialog?.dismiss()
}

private var firebaseInstance: FirebaseAuth? = null
fun getAuthInstance(): FirebaseAuth {
    if (firebaseInstance == null) {
        firebaseInstance = FirebaseAuth.getInstance()
    }
    return firebaseInstance!!
}

fun getCurrentUserId(): String{
    return FirebaseAuth.getInstance().currentUser!!.uid
}

fun changeMainFunctions(fragmentActivity: FragmentActivity, fragment: Fragment) {
    fragmentActivity.supportFragmentManager
        .beginTransaction()
        .replace(R.id.fragmentContainerView, fragment)
        .commit()
}