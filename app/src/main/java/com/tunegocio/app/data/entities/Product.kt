package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val salePrice: Double,
    val type: ProductType // INSUMO o VENTA
)

enum class ProductType {
    INSUMO, // Solo compra/inventario (no sale en ventas)
    VENTA   // Sale en ventas (puede ser simple o compuesto)
}
