package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "recipes",
    primaryKeys = ["productId", "rawMaterialId"],
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RawMaterial::class,
            parentColumns = ["id"],
            childColumns = ["rawMaterialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"]), Index(value = ["rawMaterialId"])]
)
data class Recipe(
    val productId: Int,
    val rawMaterialId: Int,
    val quantityRequired: Double
)
