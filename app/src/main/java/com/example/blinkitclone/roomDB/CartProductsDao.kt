package com.example.blinkitclone.roomDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProduct(products: CartProducts)

    @Update
    fun updateCartProduct(products: CartProducts)

    @Query("SELECT * FROM CartProducts")
    fun getAllCartProducts(): LiveData<List<CartProducts>>

    @Query("DELETE FROM CartProducts WHERE productId = :productId")
    fun deleteCartProduct(productId: String)

}