package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product_ingredients",
    primaryKeys = ["parentProductId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["parentProductId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("parentProductId"), Index("ingredientId")]
)
data class ProductIngredient(
    val parentProductId: Int, // El producto final (ej: Hamburguesa)
    val ingredientId: Int,    // El insumo (ej: Carne)
    val quantityRequired: Double
)
