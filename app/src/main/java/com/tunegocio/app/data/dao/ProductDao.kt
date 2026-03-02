package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Product
import com.tunegocio.app.data.entities.ProductIngredient
import com.tunegocio.app.data.entities.ProductType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    // ✅ Insertar ingrediente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: ProductIngredient)

    // ✅ Obtener ingredientes de un producto
    @Query("SELECT * FROM product_ingredients WHERE parentProductId = :productId")
    suspend fun getIngredients(productId: Int): List<ProductIngredient>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Product>>

     // ✅ Solo productos de VENTA (Simple o Manufacturado)
    @Query("SELECT * FROM products WHERE type IN ('PRODUCTO_SIMPLE', 'MANUFACTURADO') ORDER BY name ASC")
    suspend fun getProductsForSale(): List<Product>

    // ✅ Todos los productos (para Compras e Inventario)
    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllList(): List<Product>
    
    // ✅ Solo insumos (para crear recetas)
    @Query("SELECT * FROM products WHERE type = 'INSUMO' ORDER BY name ASC")
    suspend fun getInsumos(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Int): Product

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Product?
    
    // ✅ Borrar ingredientes anteriores al actualizar receta
    @Query("DELETE FROM product_ingredients WHERE parentProductId = :productId")
    suspend fun deleteIngredients(productId: Int)
}
