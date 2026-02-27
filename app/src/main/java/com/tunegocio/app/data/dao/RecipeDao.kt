package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.Recipe

@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: Recipe)

    @Query("""
        SELECT * FROM recipes
        WHERE productId = :productId
    """)
    suspend fun getRecipeForProduct(productId: Int): List<Recipe>
}
