package com.tunegocio.app.data.dao

import androidx.room.*
import com.tunegocio.app.data.entities.InventoryLot

@Dao
interface InventoryLotDao {

    @Insert
    suspend fun insert(lot: InventoryLot)

    @Query("SELECT * FROM inventory_lots WHERE productId = :productId")
    suspend fun getLotsForProduct(productId: Int): List<InventoryLot>

    @Query("SELECT SUM(quantity) FROM inventory_lots WHERE productId = :productId")
    suspend fun getTotalStock(productId: Int): Double?
}
