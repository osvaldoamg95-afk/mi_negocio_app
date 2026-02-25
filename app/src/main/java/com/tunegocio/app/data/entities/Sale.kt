package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val total: Double
)
