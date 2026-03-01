package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_lots",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE // Si borras producto, se borra su stock
        )
    ],
    indices = [Index(value = ["productId"]), Index(value = ["purchaseDate"])] // FIFO rápido
)
data class InventoryLot(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val quantity: Double,
    val purchasePrice: Double,
    val purchaseDate: Long
)
