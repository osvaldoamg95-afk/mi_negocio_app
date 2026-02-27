package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
