package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_details")
data class SaleDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val productId: Int,
    val quantity: Double,
    val salePrice: Double
)
