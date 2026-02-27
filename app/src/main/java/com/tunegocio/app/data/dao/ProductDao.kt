package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllList(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Int): Product
}
