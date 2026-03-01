package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["name"])] // Búsqueda rápida por nombre
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val salePrice: Double,
    val isManufactured: Boolean = false
)
