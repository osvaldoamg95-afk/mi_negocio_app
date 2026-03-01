package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Si ya existe ID, actualiza
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<Product>> // Para observar cambios en vivo

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllList(): List<Product> // Para spinners y lógica

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Int): Product

    // ✅ Búsqueda para futuro buscador (LIKE)
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchProducts(query: String): List<Product>

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Product?
}
