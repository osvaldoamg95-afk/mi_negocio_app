package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_lots")
data class InventoryLot(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val quantity: Double,
    val purchasePrice: Double,
    val purchaseDate: Long
)
