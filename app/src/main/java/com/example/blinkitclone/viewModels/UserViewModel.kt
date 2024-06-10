package com.example.blinkitclone.viewModels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blinkitclone.api.ApiUtilities
import com.example.blinkitclone.models.Product
import com.example.blinkitclone.models.Users
import com.example.blinkitclone.roomDB.CartProducts
import com.example.blinkitclone.roomDB.CartProductsDatabase
import com.example.blinkitclone.utils.Constants
import com.example.blinkitclone.utils.getCurrentUserId
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)

    val cartProductDao = CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()

    private val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    // Firebase
    fun fetchProducts(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory").child(category)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    // Room Database
    suspend fun insertCartProduct(products: CartProducts) {
        cartProductDao.insertCartProduct(products)
    }

    suspend fun updateCartProduct(products: CartProducts) {
        cartProductDao.updateCartProduct(products)
    }

    fun getAll(): LiveData<List<CartProducts>> {
        return cartProductDao.getAllCartProducts()
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductDao.deleteCartProduct(productId)
    }

    fun updateItemCount(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProducts")
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory")
            .child(product.productCategory.toString())
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductType")
            .child(product.productType.toString())
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)
    }

    fun saveUserAddress(address: String) {
        FirebaseDatabase.getInstance().getReference().child("All Users").child("Users")
            .child(getCurrentUserId()).child("userAddress").setValue(address)
    }

    // Shared Preferences
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    //Retrofit
    suspend fun checkPaymentStatus(headers: Map<String, String>) {
        val res = ApiUtilities.statusApi.checkStatus(
            headers,
            Constants.MERCHANT_ID,
            Constants.merchantTransactionId
        )
        Log.e("TAG", "checkPaymentStatus: $res", )
        _paymentStatus.value = res.body() != null && res.body()!!.success
    }

}