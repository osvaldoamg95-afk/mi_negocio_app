package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["name"])]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val salePrice: Double, // 0.0 si es INSUMO
    val type: ProductType
)

enum class ProductType {
    INSUMO,
    PRODUCTO_SIMPLE,
    MANUFACTURADO
}
