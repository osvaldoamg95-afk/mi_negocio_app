package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.RawMaterial

@Dao
interface RawMaterialDao {

    @Insert
    suspend fun insert(material: RawMaterial)

    @Query("SELECT * FROM raw_materials")
    suspend fun getAll(): List<RawMaterial>
}
