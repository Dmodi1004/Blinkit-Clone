package com.example.blinkitclone.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.blinkitclone.adapters.CartProductsAdapter
import com.example.blinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.blinkitclone.databinding.AddressLayoutBinding
import com.example.blinkitclone.utils.Constants
import com.example.blinkitclone.utils.hideDialog
import com.example.blinkitclone.utils.showToast
import com.example.blinkitclone.viewModels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {

    private val binding: ActivityOrderPlaceBinding by lazy {
        ActivityOrderPlaceBinding.inflate(layoutInflater)
    }

    private val viewModel: UserViewModel by viewModels()
    private lateinit var cartProductsAdapter: CartProductsAdapter
    private lateinit var b2BPGRequest: B2BPGRequest
    private val B2B_PG_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getAllCartProducts()
        initializePhonePe()
        onPlaceHolderClick()

        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            finish()
        }

    }

    private fun initializePhonePe() {
        val data = JSONObject()

        PhonePe.init(this, PhonePeEnvironment.SANDBOX, Constants.MERCHANT_ID, "")

        data.put("merchantId", Constants.MERCHANT_ID)
        data.put("merchantTransactionId", Constants.merchantTransactionId)
        data.put("merchantUserId", "90223250")
        data.put("amount", 1000)
        data.put("mobileNumber", "9999999999")
        data.put("callbackUrl", "https://webhook.site/callback-url")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")
        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")
        data.put("deviceContext", deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), Base64.NO_WRAP
        )
        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1"

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }
    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    private fun onPlaceHolderClick() {
        binding.nextBtn.setOnClickListener {
            viewModel.getAddressStatus().observe(this) { status ->
                if (status) {
                    getPaymentView()
                } else {
                    val addressLayoutBinding =
                        AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alertDialog.show()
                    addressLayoutBinding.addBtn.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding)
                    }
                }
            }
        }
    }
    private fun checkStatus() {
        val xVerify =
            sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID
        )
        lifecycleScope.launch {
            viewModel.checkPaymentStatus(headers)
            viewModel.paymentStatus.collect { status ->
                if (status) {
                    showToast(this@OrderPlaceActivity, "Payment Done 👍")
                    startActivity(Intent(this@OrderPlaceActivity, UsersActivity::class.java))
                    finish()
                } else {
                    showToast(this@OrderPlaceActivity, "Payment Failed 👎")
                }
                Log.e("TAG", "checkStatus: $status", )
            }
        }
    }

    private fun getPaymentView() {
        try {
            startActivityForResult(
                PhonePe.getImplicitIntent(
                    this,
                    b2BPGRequest,
                    "com.phonepe.simulator"
                )!!, B2B_PG_REQUEST_CODE
            );
        } catch (e: PhonePeInitException) {
            showToast(this, e.message.toString())
        }
    }
    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        com.example.blinkitclone.utils.showDialog(this, "Processing...")

        val userPinCode = addressLayoutBinding.pinCodeEdt.text.toString()
        val userPhoneNumber = addressLayoutBinding.phoneNumberEdt.text.toString()
        val userState = addressLayoutBinding.stateEdt.text.toString()
        val userDistrict = addressLayoutBinding.districtEdt.text.toString()
        val userAddress = addressLayoutBinding.descriptiveAddressEdt.text.toString()

        val address = "$userAddress, $userPinCode, $userDistrict($userState), $userPhoneNumber"

        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }
        showToast(this, "Saved")
        alertDialog.dismiss()
        hideDialog()
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) { cartProductsList ->
            cartProductsAdapter = CartProductsAdapter()
            binding.productsItemRv.adapter = cartProductsAdapter
            cartProductsAdapter.differ.submitList(cartProductsList)

            var totalPrice = 0

            for (products in cartProductsList) {
                val price = products.productPrice?.substring(1)?.toInt()
                val itemCount = products.productCount!!
                totalPrice += (price?.times(itemCount)!!)

            }
            binding.subTotalTv.text = totalPrice.toString()

            if (totalPrice < 200) {
                binding.deliveryChargesTv.text = "₹15"
                totalPrice += 15
            }
            binding.grandTotalTv.text = totalPrice.toString()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == B2B_PG_REQUEST_CODE) {
            checkStatus()
        }
        Log.e("TAG", "onActivityResult: $requestCode", )
    }


}