package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_details",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE // Si borras la venta, se van los detalles
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT // No puedes borrar producto si ya se vendió (seguridad histórica)
        )
    ],
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])]
)
data class SaleDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val productId: Int,
    val quantity: Double,
    val salePrice: Double
)
