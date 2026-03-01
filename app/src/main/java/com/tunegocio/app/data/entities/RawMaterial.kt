package com.tunegocio.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "raw_materials",
    indices = [Index(value = ["name"])]
)
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
